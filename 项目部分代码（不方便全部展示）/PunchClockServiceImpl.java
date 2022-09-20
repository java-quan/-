package com.yuhong.labour.core.service.punchClock.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuhong.labour.core.entity.fee.WeekAlloc;
import com.yuhong.labour.core.entity.project.ProjectEntity;
import com.yuhong.labour.core.entity.punchClock.PunchClockEntity;
import com.yuhong.labour.core.mapper.project.ProjectMapper;
import com.yuhong.labour.core.mapper.punchClock.PunchClockMapper;
import com.yuhong.labour.core.request.punchClock.*;
import com.yuhong.labour.core.service.project.ProjectService;
import com.yuhong.labour.core.service.punchClock.PunchClockService;
import com.yuhong.labour.core.util.CharEscapeUtil;
import com.yuhong.labour.core.vo.fee.WeekAllocVO;
import com.yuhong.labour.core.vo.punchClock.*;
import com.yuhong.labour.user.feign.LabourUserGroupClient;
import com.yuhong.labour.user.feign.LabourUserInfoClient;
import com.yuhong.labour.user.vo.GroupInfoVo;
import com.yuhong.labour.user.vo.UserInfoVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.apache.rocketmq.client.producer.SendStatus.SEND_OK;

import static org.apache.rocketmq.client.producer.SendStatus.SEND_OK;

/**
 * redis存储说明
 * 用户打卡信息存储  key:labour_core_时间(20220725)_userid value:punchclockentity
 */
@Slf4j
@Service
@AllArgsConstructor
public class PunchClockServiceImpl extends ServiceImpl<PunchClockMapper, PunchClockEntity> implements PunchClockService {

    private RedisTemplate<String, String> redisTemplate;

    private DefaultMQProducer producer;

    private PunchClockMapper punchClockMapper;

    private LabourUserInfoClient labourUserInfoClient;

    private ProjectMapper projectMapper;


    /**
     * 将消息存到redis和rocketmq中
     *
     * @param punchClockEntity
     * @return
     */
    @Override
    public R punchSendMq(PunchClockEntity punchClockEntity) {

//        if (labourUserInfoClient.getUserAuth3(punchClockEntity.getUserId()).getData() == null) {
//            return R.fail(9001, "未通过三要素校验");
//        }
        /**
         * 生成标示符例如（20220808+123456+0或者1）
         * 第一段为日期字符串 第二段为userId 最后一个数字是表示上班还是下班
         */
        punchClockEntity.setTimeIdentification(DateUtil.format(new Date(), "yyyyMMdd") + punchClockEntity.getUserId().toString() + punchClockEntity.getPunchStatus());
        Integer retryCount = 0;
////        首先解决以项目为中心的个人打卡查询 需要用redis list结构
//        ListOperations listOperations = redisTemplate.opsForList();
//        String redisKey="appUserPunch:"+DateUtil.format(new Date(),"yyyyMMdd")+":"+punchClockEntity.getProjectId()+":"+punchClockEntity.getUserId();
//        listOperations.rightPush(redisKey,JSON.toJSON(punchClockEntity));
//        log.info(" userId: {} 个人打卡信息存放redis中",punchClockEntity.getUserId());
//        项目打卡记录存到redis

//        for(int i=0;i<10000;i++){
//            PunchClockEntity punchClockEntity1=new PunchClockEntity();
//            punchClockEntity1.setUserId((long)i);
//            String body1 = JSON.toJSONString(punchClockEntity1);
//            Message msg = new Message("TopicA", body1.getBytes());
//            pushMQ(msg,retryCount);
//        }
        String body = JSON.toJSONString(punchClockEntity);
        Message msg = new Message("Labour-PunchClock", body.getBytes());
        punchClockMapper.insert(punchClockEntity);
        pushMQ(msg, retryCount);  //先注释一下mq
        return R.success("打卡成功");
    }

    /**
     * 重试一次 如果异常则插入重试数据库
     *
     * @param msg
     * @param retryCount
     */
    public void pushMQ(Message msg, Integer retryCount) {
        if (retryCount >= 2) {
            PunchClockEntity punchClockEntity = JSON.parseObject(msg.getBody(), PunchClockEntity.class);
            log.info(" {} 消息发送失败", punchClockEntity);
            List<PunchClockEntity> list = new ArrayList<>();
            list.add(punchClockEntity);
            punchClockMapper.insertRetryBatch(list);
            return;
        }
        try {
            SendResult result = producer.send(msg);
            log.info("发送消息 状态 {}", result.getSendStatus());
            if (result.getSendStatus() == SEND_OK) {
                return;
            } else {
                retryCount++;
                pushMQ(msg, retryCount);
            }

        } catch (Exception e) {
            retryCount++;
            pushMQ(msg, retryCount);
        }
    }

    @Override
    public Integer insertBatch(List<PunchClockEntity> entities) {
        return punchClockMapper.insertBatch(entities);
    }


    @Override
    public Integer insertBatch1(List<PunchClockEntity> entities, String tableName) {
        return punchClockMapper.insertBatch1(entities, tableName);
    }

    @Override
    public List<PunchClockEntity> selectRetry() {
        return punchClockMapper.selectRetry();
    }


    @Override
    public R<IPage<PunchClockVO>> findProjectPunch(Query query, PunchProjectClockReq punchProjectClockReq) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        if (punchProjectClockReq.getProjectName() != null && punchProjectClockReq.getProjectName() != "") {
            queryWrapper.like("p.project_name", punchProjectClockReq.getProjectName());
        }
        if (punchProjectClockReq.getProjectCode() != null && punchProjectClockReq.getProjectCode() != "") {
            queryWrapper.like("projectCode", punchProjectClockReq.getProjectCode());
        }
        Page<PunchClockVO> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<PunchClockVO> punchClockVOIPage = this.punchClockMapper.selectProjectPunch(queryWrapper, page);
        return R.data(punchClockVOIPage);
    }

    @Override
    public R<IPage<PunchUserClockVO>> findUserPunch(Query query, PunchUserClockReq punchUserClockReq) {
        QueryWrapper<PunchUserClockVO> queryWrapper = new QueryWrapper<>();
        if (punchUserClockReq.getProjectName() != " " && punchUserClockReq.getProjectName() != null) {
            queryWrapper.like("lp.project_name", punchUserClockReq.getProjectName());
        }
        if (punchUserClockReq.getProjectName() != " " && punchUserClockReq.getProjectCode() != null) {
            queryWrapper.like("lp.project_code", punchUserClockReq.getProjectName());
        }
        Page<PunchUserClockVO> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<PunchUserClockVO> punchUserClockVOIPage = this.punchClockMapper.selectUserPunch(queryWrapper, page);
        return R.data(punchUserClockVOIPage);
    }

    @Override
    public R<IPage<PunchTeamClockVO>> findTeamPunch(Query query, PunchTeamClockReq punchTeamClockReq) {
        QueryWrapper<PunchTeamClockVO> queryWrapper = new QueryWrapper<>();
        if (punchTeamClockReq.getProjectName() != " " && punchTeamClockReq.getProjectName() != null) {
            queryWrapper.like("lp.project_name", punchTeamClockReq.getProjectName());
        }
        if (punchTeamClockReq.getProjectName() != " " && punchTeamClockReq.getProjectCode() != null) {
            queryWrapper.like("lp.project_code", punchTeamClockReq.getProjectName());
        }
        Page<PunchTeamClockVO> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<PunchTeamClockVO> punchUserClockVOIPage = this.punchClockMapper.selectTeamPunch(queryWrapper, page);
        return R.data(punchUserClockVOIPage);
    }

    @Override
    public R<IPage<PunchAttendanceRecordClockVO>> findAttendanceRecordPunch(Query query, PunchAttendanceRecordClockReq punchAttendanceRecordClockReq) {
        QueryWrapper<PunchAttendanceRecordClockVO> queryWrapper = new QueryWrapper<>();
        if (punchAttendanceRecordClockReq.getProjectId() != null) {
            queryWrapper.like("lp.id", punchAttendanceRecordClockReq.getProjectId());
        }
        if (punchAttendanceRecordClockReq.getTeamId() != null) {
            queryWrapper.like("pc.team_id", punchAttendanceRecordClockReq.getTeamId());
        }
        if (punchAttendanceRecordClockReq.getWBS() != null && punchAttendanceRecordClockReq.getWBS() != " ") {
            queryWrapper.like("lp.WBS", punchAttendanceRecordClockReq.getTeamId());
        }
        Page<PunchAttendanceRecordClockVO> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<PunchAttendanceRecordClockVO> punchUserClockVOIPage = this.punchClockMapper.selectAttendanceRecordPunch(queryWrapper, page);
        List<PunchAttendanceRecordClockVO> records = punchUserClockVOIPage.getRecords();
        Set<Long> userSet = new HashSet<>();
        for (PunchAttendanceRecordClockVO record : records) {
            userSet.add(record.getUserId());
        }
        List<UserInfoVo> userDataList = labourUserInfoClient.getUserInfoByIds(new ArrayList<>(userSet)).getData();
        HashMap<Long, String> userHashMap = new HashMap<>();
        HashMap<Long, String> groupHashMap = new HashMap<>();
        for (UserInfoVo userData : userDataList) {
            userHashMap.put(userData.getId(), userData.getName());
            groupHashMap.put(userData.getId(), userData.getGroupName());
        }
        for (PunchAttendanceRecordClockVO record : records) {
            record.setUserName(userHashMap.get(record.getUserId()));
            record.setTeamName(groupHashMap.get(record.getUserId()));
        }
        punchUserClockVOIPage.setRecords(records);
        return R.data(punchUserClockVOIPage);
    }

    @Override
    public R<IPage<AppPunchTeamClockVO>> findAppPunchTeamList(Query query, AppPunchTeamClockReq appPunchTeamClockReq) {
        Page<AppPunchTeamClockVO> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<AppPunchTeamClockVO> data = punchClockMapper.selectAppPunchTeamList(appPunchTeamClockReq, page);
        List<AppPunchTeamClockVO> records = data.getRecords();
        Set<Long> userSet = new HashSet<>();
        for (AppPunchTeamClockVO record : records) {
            userSet.add(record.getUserId());
        }
        List<UserInfoVo> userDataList = labourUserInfoClient.getUserInfoByIds(new ArrayList<>(userSet)).getData();
        HashMap<Long, String> userHashMap = new HashMap<>();
        for (UserInfoVo userData : userDataList) {
            userHashMap.put(userData.getId(), userData.getName());
        }
        for (AppPunchTeamClockVO record : records) {
            record.setUserName(userHashMap.get(record.getUserId()));
        }
        data.setRecords(records);
        return R.data(data);
    }


    @Override
    public R<IPage<AppPunchProjectClockVO>> findAppPunchProjectList(Query query, AppPunchProjectClockReq appPunchProjectClockReq) {
        Page<AppPunchProjectClockVO> page = new Page<>(query.getCurrent(), query.getSize());
//        appPunchProjectClockReq.setEndDate(DateUtil.format(DateUtil.offsetDay(DateUtil.parse(appPunchProjectClockReq.getEndDate()),1),"yyyy-MM-dd"));
        IPage<AppPunchProjectClockVO> data = punchClockMapper.selectAppPunchProjectList(appPunchProjectClockReq, page);
        List<AppPunchProjectClockVO> records = data.getRecords();
        Set<Long> userSet = new HashSet<>();
        Set<Long> groupSet = new HashSet<>();
        for (AppPunchProjectClockVO record : records) {
            userSet.add(record.getUserId());
            groupSet.add(record.getTeamId());
        }
        List<UserInfoVo> userDataList = labourUserInfoClient.getUserInfoByIds(new ArrayList<>(userSet)).getData();
        HashMap<Long, String> userHashMap = new HashMap<>();
        HashMap<Long, String> groupHashMap = new HashMap<>();
        for (UserInfoVo userData : userDataList) {
            userHashMap.put(userData.getId(), userData.getName());
            groupHashMap.put(userData.getId(), userData.getGroupName());
        }


        for (AppPunchProjectClockVO record : records) {
            record.setUserName(userHashMap.get(record.getUserId()));
            record.setTeamName(groupHashMap.get(record.getUserId()));
        }
        data.setRecords(records);
        return R.data(data);
    }

    @Override
    public R userExcel(List<Long> ids) {
        List<PunchUserClockVO> punchUserClockVOList = punchClockMapper.selectUserBatch(ids);
        String PATH = "C:\\Users\\Administrator\\Desktop\\" + System.currentTimeMillis() + ".xlsx";  //路径
        EasyExcel.write(PATH, PunchUserClockVO.class).sheet("模块1").doWrite(punchUserClockVOList);
        return R.success("导出成功");
    }

    @Override
    public R projectExcel(List<Long> ids) {
        List<PunchAttendanceRecordClockVO> punchAttendanceRecordClockVOS = punchClockMapper.selectProjectBatch(ids);
        String PATH = "C:\\Users\\Administrator\\Desktop\\" + System.currentTimeMillis() + ".xlsx";  //路径
        EasyExcel.write(PATH, PunchAttendanceRecordClockVO.class).sheet("模块1").doWrite(punchAttendanceRecordClockVOS);
        return R.success("导出成功");
    }

    @Override
    public R teamExcel(List<Long> ids) {
        List<PunchAttendanceRecordClockVO> punchAttendanceRecordClockVOS = punchClockMapper.selectTeamBatch(ids);
        String PATH = "C:\\Users\\Administrator\\Desktop\\" + System.currentTimeMillis() + ".xlsx";  //路径
        EasyExcel.write(PATH, PunchAttendanceRecordClockVO.class).sheet("模块1").doWrite(punchAttendanceRecordClockVOS);
        return R.success("导出成功");
    }

    @Override
    public List<WeekAlloc> weekAllocList() {
        return punchClockMapper.weekAllocLists();
    }

    @Override
    public R<IPage<AppPunchProjectRecordVO>> findAppPunchProjectRecordList(Query query, AppPunchProjectRecordClockVO appPunchProjectRecordClockVO) {
        Page<AppPunchProjectRecordVO> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<AppPunchProjectRecordVO> data = punchClockMapper.selectAppPunchProjectRecordList(page, appPunchProjectRecordClockVO);
        return R.data(data);
    }

    @Override
    public List<EmpPunchClockVO> findEmpPunchClock(PunchEmpReq punchEmpReq) {
        List<EmpPunchClockVO> empPunchClockVO = punchClockMapper.selectEmpPunchClock(punchEmpReq);
        for (EmpPunchClockVO punchClockVO : empPunchClockVO) {
            Long id = projectMapper.findProjectByWES(punchEmpReq.getWbs()).getId();
            for (WorkerVO workerVO : punchClockVO.getWorkerList()) {
                Map<String, Object> map = new HashedMap();
                map.put("projectId", id);
                map.put("userId", workerVO.getUserId());
                map.put("startTime", punchEmpReq.getStartDateTime());
                map.put("endTime", punchEmpReq.getEndDateTime());
                List<AppPunchProjectRecordClockVO> appPunchProjectRecordClockVOS = punchClockMapper.selectPunchCount(map);
                if (appPunchProjectRecordClockVOS.isEmpty() && appPunchProjectRecordClockVOS.get(0).getProjectId() == 2L) {
                    Map<String, Object> hashedMap = new HashedMap();
                    hashedMap.put("times", appPunchProjectRecordClockVOS.get(0).getTimes());
                    hashedMap.put("projectId", id);
                    hashedMap.put("userId", workerVO.getUserId());
                    List<Long> list = punchClockMapper.selectProjectId(hashedMap);
                    if (!list.isEmpty()) {
                        Long projectId = list.get(0);
                        if (id == projectId) {
                            workerVO.setConfirmFlag(2);
                        }
                    }
                } else {
                    if (workerVO.getStartTime() != null && workerVO.getEndTime() != null) {
                        if (workerVO.getAffirmStatus() == 0) {
                            workerVO.setConfirmFlag(0);
                        } else if (workerVO.getAffirmStatus() == 1) {
                            if (workerVO.getAffirmWay() == 0) {
                                workerVO.setConfirmFlag(1);
                            } else {
                                workerVO.setConfirmFlag(10);
                            }
                        } else {
                            workerVO.setConfirmFlag(-1);
                        }
                    } else if (workerVO.getStartTime() == null || workerVO.getEndTime() == null) {
                        workerVO.setConfirmFlag(9);
                    }
                }
            }
        }
        return empPunchClockVO;
    }

    @Override
    public R<IPage<AppPunchUserClockVO>> findAppPunchUserList(Query query, AppPunchUserClockReq appPunchUserClockReq) {
        Page<AppPunchUserClockVO> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<AppPunchUserClockVO> data = punchClockMapper.selectAppPunchUserList(page, appPunchUserClockReq);
        List<AppPunchUserClockVO> records = data.getRecords();
        Set<Long> userSet = new HashSet<>();
        for (AppPunchUserClockVO record : records) {
            userSet.add(record.getUserId());
        }
        List<UserInfoVo> userDataList = labourUserInfoClient.getUserInfoByIds(new ArrayList<>(userSet)).getData();
        HashMap<Long, String> userHashMap = new HashMap<>();
        for (UserInfoVo userData : userDataList) {
            userHashMap.put(userData.getId(), userData.getName());
        }
        for (AppPunchUserClockVO record : records) {
            record.setUserName(userHashMap.get(record.getUserId()));
        }
        data.setRecords(records);
        return R.data(data);
    }

    @Override
    public R<IPage<AppPunchWorkersClockVO>> findAppPunchWorkersList(Query query, AppPunchWorkersClockReq appPunchWorkersClockReq) {
        appPunchWorkersClockReq.setUserName(CharEscapeUtil.charEscape(appPunchWorkersClockReq.getUserName()));
        Page<AppPunchWorkersClockVO> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<AppPunchWorkersClockVO> data = punchClockMapper.selectAppPunchWorkersList(page, appPunchWorkersClockReq);
//        List<AppPunchWorkersClockVO> records = data.getRecords();
//        Set<Long> userSet = new HashSet<>();
//        for (AppPunchWorkersClockVO record : records) {
//            userSet.add(record.getUserId());
//        }
//        List<UserInfoVo> userDataList = labourUserInfoClient.getUserInfoByIds(new ArrayList<>(userSet)).getData();
//        HashMap<Long,String> userHashMap = new HashMap<>();
//        for (UserInfoVo userData : userDataList) {
//            userHashMap.put(userData.getId(),userData.getName());
//        }
//        for (AppPunchWorkersClockVO record : records) {
//            record.setUserName(userHashMap.get(record.getUserId()));
//        }
//        data.setRecords(records);
        return R.data(data);
    }

    @Override
    public R findAppPunchDataBoardList(AppPunchDataBoardReq appPunchWorkersClockReq) {
        return R.data(punchClockMapper.selectAppPunchDataBoardList(appPunchWorkersClockReq));
    }

    @Override
    public R<IPage<AppAutonomouslyClockVO>> findAppPunchAutonomouslyList(Query query, AppAutomonouslyClockReq appAutomonouslyClockReq) {
        Page<AppAutonomouslyClockVO> page = new Page<>(query.getCurrent(), query.getSize());
        return R.data(punchClockMapper.selectAppAutonomouslyList(page, appAutomonouslyClockReq));
    }

    @Override
    public R modifiedAffirmById(Long id, Long projectId,String time) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", id);
        if (time ==null || time ==" ") {
             time = DateUtil.format(new Date(), "yyyyMMdd");
        }else {
           time = DateUtil.format(DateUtil.parse(time).toJdkDate(),"yyyyMMdd");
        }
        map.put("ciTime", time + id + "0");
        map.put("coTime", time + id + "1");
        map.put("projectId", projectId);
        AppPunchUserClockVO ci = punchClockMapper.selectAffirmCiById(map);
        AppPunchUserClockVO co = punchClockMapper.selectAffirmCoById(map);
        PunchClockEntity punchClockEntity = new PunchClockEntity();
        punchClockEntity.setAffirmStatus(1);
        punchClockEntity.setAffirmWay(0);
        if (ci != null) {
            punchClockMapper.update(punchClockEntity, new QueryWrapper<PunchClockEntity>()
                    .eq("user_id", ci.getUserId())
                    .eq("project_id", ci.getProjectId())
                    .eq("punch_time", ci.getDateNow()));
            punchClockMapper.update(punchClockEntity, new QueryWrapper<PunchClockEntity>()
                    .eq("user_id", ci.getUserId())
                    .eq("project_id", ci.getProjectId())
                    .eq("punch_time", co.getDateNow()));
        }
        return R.status(true);
    }

    @Override
    public R confirm(PunchManagerReq punchManagerReq) {
        ProjectEntity projectByWES = projectMapper.findProjectByWES(punchManagerReq.getWbs());
        if (punchManagerReq.getUserList() == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("projectId", projectByWES.getId());
            map.put("times", punchManagerReq.getTime());
            List<AppPunchUserClockVO> ci = punchClockMapper.selectAllUserPunchClockCi(map);
            List<AppPunchUserClockVO> co = punchClockMapper.selectAllUserPunchClockCo(map);
            PunchClockEntity punchClockEntity = new PunchClockEntity();
            punchClockEntity.setAffirmStatus(Integer.parseInt(punchManagerReq.getType()));
            punchClockEntity.setAffirmWay(0);
            for (AppPunchUserClockVO appPunchUserClockVO : ci) {
                if (appPunchUserClockVO != null) {
                    punchClockMapper.update(punchClockEntity, new QueryWrapper<PunchClockEntity>()
                            .eq("user_id", appPunchUserClockVO.getUserId())
                            .eq("project_id", appPunchUserClockVO.getProjectId())
                            .eq("punch_time", appPunchUserClockVO.getDateNow()));
                }
            }
            for (AppPunchUserClockVO appPunchUserClockVO : co) {
                if (appPunchUserClockVO != null) {
                    punchClockMapper.update(punchClockEntity, new QueryWrapper<PunchClockEntity>()
                            .eq("user_id", appPunchUserClockVO.getUserId())
                            .eq("project_id", appPunchUserClockVO.getProjectId())
                            .eq("punch_time", appPunchUserClockVO.getDateNow()));
                }
            }
        } else {
            String[] users = punchManagerReq.getUserList().split(",");
            for (int i = 0; i < users.length; i++) {
                Long userId = Long.getLong(users[i]);
                modifiedAffirmById(userId,projectByWES.getId(),punchManagerReq.getTime());
            }
        }
        return R.status(true);
    }

    @Override
    public List<PunchUserIdAndProjectId> userIdAndProjectIdAffirm(List<PunchUserIdAndProjectId> appPunchUserClockReq) {
        List<Long> userList = new ArrayList<>();
        List<Long> projectList = new ArrayList<>();
        for (PunchUserIdAndProjectId punchUserClockReq : appPunchUserClockReq) {
            userList.add(punchUserClockReq.getUserId());
            projectList.add(punchUserClockReq.getProjectId());
        }
        return punchClockMapper.selectPunchAffirmUserIdAndProjectId(userList,projectList);
    }


}

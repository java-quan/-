package com.yuhong.labour.core.service.insurance.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuhong.labour.core.constant.OperationConstant;
import com.yuhong.labour.core.entity.insurance.*;
import com.yuhong.labour.core.mapper.insurance.ClaimMapper;
import com.yuhong.labour.core.mapper.insurance.ClaimOssMapper;
import com.yuhong.labour.core.mapper.insurance.ClaimStatusMapper;
import com.yuhong.labour.core.mapper.insurance.OperationLogMapper;
import com.yuhong.labour.core.request.insurance.ClaimReq;
import com.yuhong.labour.core.request.insurance.ClaimStatusReq;
import com.yuhong.labour.core.service.insurance.ClaimService;
import com.yuhong.labour.core.service.insurance.InsuranceService;
import com.yuhong.labour.core.service.insurance.OperationLogService;
import com.yuhong.labour.core.util.AliyunOSSUtil;
import com.yuhong.labour.core.util.CharEscapeUtil;
import com.yuhong.labour.core.util.RandomNumberUtils;
import com.yuhong.labour.user.feign.IPushClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.yuhong.labour.core.constant.OperationConstant.*;

@Service
@AllArgsConstructor
@Slf4j
public class ClaimServiceImpl extends ServiceImpl<ClaimMapper , ClaimEntity> implements ClaimService {

    private ClaimMapper claimMapper;
    private ClaimStatusMapper claimStatusMapper;
    private ClaimOssMapper claimOssMapper;
    private AliyunOSSUtil aliyunOSSUtil;
    private IPushClient iPushClient;
    private OperationLogService operationLogService;
    private InsuranceService insuranceService;

    @Override
    public R findClaimAndOss(Long id) {
        return R.data(claimMapper.selectClaimAndOss(id));
    }

    @Override
    public R findClaimAndOssAndStatus(Long id) {
        return R.data(claimMapper.selectClaimAndOssAndStatus(id));
    }

    @Override
    public R<IPage<ClaimEntity>> page(ClaimReq claimReq, Query query) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if(claimReq.getClaimUserName()!=null && claimReq.getClaimUserName()!= " "){
            String insurerName = CharEscapeUtil.charEscape(claimReq.getClaimUserName().toString());
            queryWrapper.like("claim_user_name",insurerName);
        }
        if (claimReq.getClaimInsuranceName()!=null && claimReq.getClaimInsuranceName()!= " "){
            String claimInsuranceName = CharEscapeUtil.charEscape(claimReq.getClaimInsuranceName().toString());
            queryWrapper.like("claim_insurance_name",claimInsuranceName);
        }
        if (claimReq.getClaimStatus()!=null){
            queryWrapper.eq("claim_status",claimReq.getClaimStatus());
        }
        IPage<ClaimEntity> pages =claimMapper.selectPage(Condition.getPage(query), queryWrapper);
        return R.data(pages);
    }

    @Override
    public R saves(ClaimEntity claimEntity) {
        try {
            int day = RandomNumberUtils.DaysNumber(claimEntity.getInjuredTime());
            DateTime offset = DateUtil.offset(claimEntity.getGmtCreated(), DateField.DAY_OF_YEAR, -1);
//            String startTime = offset.toString("yyyy-MM-dd HH:mm:ss");
//            String endTime = DateUtil.format(claimEntity.getGmtCreated(), "yyyy-MM-dd HH:mm:ss");
//            OperationEntity operationEntity =new OperationEntity();
//            operationEntity.setUserId(claimEntity.getClaimUserId());
//            operationEntity.setStartTime(startTime);
//            operationEntity.setEndTime(endTime);
//            operationEntity.setOperationType(0);
//            if (operationLogService.findOperation(operationEntity).getData()){
//                return R.fail("申请理赔失败");
//            }
            if (DAY_COUNT<day){
                return R.fail("申请理赔失败");
            }

            // TODO: 2022/8/31 判断这个用户保险金额够不够买保险合同到期之前的所有天数
            BigDecimal bigDecimal =null;


            boolean flag = false;

            List<ClaimStatusEntity> claimStatusEntityList = new ArrayList<>();
            ClaimStatusEntity claimStatusEntity = new ClaimStatusEntity();
            claimStatusEntity.setClaimId(claimEntity.getId());
            claimStatusEntity.setStatus(0);
            claimStatusEntity.setDetail("被保险人提交申请");
            claimStatusEntityList.add(claimStatusEntity);
            if (claimEntity.getClaimClassify()==RIDER_SIX_TYPE){
                ClaimStatusEntity newClaimStatusEntity = new ClaimStatusEntity();
                newClaimStatusEntity.setClaimId(claimEntity.getId());
                newClaimStatusEntity.setStatus(8);
                newClaimStatusEntity.setDetail("等待邮寄材料");
                claimStatusEntityList.add(newClaimStatusEntity);
            }else {
                ClaimStatusEntity newClaimStatusEntity = new ClaimStatusEntity();
                newClaimStatusEntity.setClaimId(claimEntity.getId());
                newClaimStatusEntity.setStatus(1);
                newClaimStatusEntity.setDetail("等待上传相关资料");
                claimStatusEntityList.add(newClaimStatusEntity);
            }
            for (int i = 0; i < claimStatusEntityList.size(); i++) {
                this.claimStatusMapper.insert(claimStatusEntityList.get(i));
            }
            for (int i = 0; i < claimEntity.getOssList().size(); i++) {
                claimEntity.getOssList().get(i).setClaimId(claimEntity.getId());
                claimEntity.getOssList().get(i).setClaimType(ZERO_TYPE);
            }
//        存入oss理赔记录表
            for (int i = 0; i < claimEntity.getOssList().size(); i++) {
                this.claimOssMapper.insert(claimEntity.getOssList().get(i));
            }
            flag=true;
            claimEntity.setClaimStatus(1);
            claimMapper.insert(claimEntity);
            InsuranceEntity insuranceEntity = new InsuranceEntity();
            insuranceEntity.setLockType(ONE_TYPE);
            insuranceService.update(insuranceEntity,new QueryWrapper<InsuranceEntity>().eq("insurer_id",claimEntity.getClaimUserId()));
            log.info("申请理赔成功");
            return R.status(flag);
        }catch ( ParseException p){
            log.error("服务目前繁忙,理赔失败:{}",p);
            return R.status(false);
        }
    }

    @Override
    public R revocation(ClaimEntity claimEntity)  {
        List<Long> list = new ArrayList<>();
        list.add(claimEntity.getClaimUserId());
        try {
            claimEntity.setIsDeleted(ONE_TYPE);
            // todo 撤销的时候看看申请时有没有扣钱  如果扣钱了的话需要往账户退款
            int count = claimMapper.updateById(claimEntity);
            if (count>0){
                iPushClient.pushMessage(list,null,"理赔撤销成功","您的撤销理赔申请已通过，现已终止理赔申请。",1);
            }
            return R.success("撤销理赔成功");
        }catch (Exception e){
            iPushClient.pushMessage(list,null,"理赔撤销失败","您的撤销理赔申请被拒绝，可点击查看详情。",1);
            return R.fail("撤销理赔失败");
        }
            }
    @Override
    public R update(ClaimStatusReq claimStatusReq) {
        List<Long> list = new ArrayList<>();
        list.add(claimStatusReq.getId());
        if (claimStatusReq.getStatus()==2){
            List<ClaimStatusEntity> claimStatusEntityList = new ArrayList<>();
            ClaimStatusEntity newClaimStatusEntity = new ClaimStatusEntity();
            newClaimStatusEntity.setClaimId(claimStatusReq.getId());
            newClaimStatusEntity.setStatus(2);
            newClaimStatusEntity.setDetail("等待理赔专员进行审核");
            if (!claimStatusReq.getOssList().isEmpty()) {
                for (ClaimOssEntity claimOssEntity : claimStatusReq.getOssList()) {
                    claimOssMapper.insert(claimOssEntity);
                }
            }
            claimStatusEntityList.add(newClaimStatusEntity);
            for (int i = 0; i < claimStatusEntityList.size(); i++) {
                claimStatusMapper.insert(claimStatusEntityList.get(i));
            }
        }else if(claimStatusReq.getStatus()==5){
            ClaimStatusEntity claimStatusEntity = new ClaimStatusEntity();
            claimStatusEntity.setClaimId(claimStatusReq.getId());
            claimStatusEntity.setStatus(5);
            claimStatusEntity.setDetail("审核通过");
            this.claimStatusMapper.insert(claimStatusEntity);
            iPushClient.pushMessage(list,null,"理赔证明审核通过","您的理赔申请材料已通过初审，最终审核的结果以邮寄材料后，保险公司的审核为准，请知晓。",1);
        }else if(claimStatusReq.getStatus()==3){
            List<ClaimStatusEntity> claimStatusEntityList = new ArrayList<>();
            ClaimStatusEntity claimStatusEntity = new ClaimStatusEntity();
            claimStatusEntity.setClaimId(claimStatusReq.getId());
            claimStatusEntity.setStatus(3);
            claimStatusEntity.setDetail("审核未通过");
            ClaimStatusEntity newClaimStatusEntity = new ClaimStatusEntity();
            newClaimStatusEntity.setClaimId(claimStatusReq.getId());
            newClaimStatusEntity.setStatus(4);
            newClaimStatusEntity.setDetail("修改中");
            claimStatusEntityList.add(claimStatusEntity);
            claimStatusEntityList.add(newClaimStatusEntity);
            for (int i = 0; i < claimStatusEntityList.size(); i++) {
                claimStatusMapper.insert(claimStatusEntityList.get(i));
            }
            ClaimEntity claimEntity = new ClaimEntity();
            claimEntity.setId(claimStatusReq.getId());
            claimEntity.setClaimStatus(4);
            iPushClient.pushMessage(list,null,"理赔证明审核失败","您的理赔申请材料未通过审核，可点击查看详情。",1);
            return R.status(claimMapper.updateById(claimEntity)>0);
        }else if (claimStatusReq.getStatus()==6){
            ClaimStatusEntity claimStatusEntity = new ClaimStatusEntity();
            claimStatusEntity.setClaimId(claimStatusReq.getId());
            claimStatusEntity.setStatus(6);
            claimStatusEntity.setDetail("理赔成功");
            claimStatusMapper.insert(claimStatusEntity);
            iPushClient.pushMessage(list,null,"理赔完成","您的理赔已完成，本次理赔金额为XXX.XX元。",1);
        }else if (claimStatusReq.getStatus()==7){
            ClaimStatusEntity claimStatusEntity = new ClaimStatusEntity();
            claimStatusEntity.setClaimId(claimStatusReq.getId());
            claimStatusEntity.setStatus(7);
            claimStatusEntity.setDetail("理赔失败");
            claimStatusMapper.insert(claimStatusEntity);
        }
        ClaimEntity claimEntity = new ClaimEntity();
        claimEntity.setId(claimStatusReq.getId());
        claimEntity.setClaimStatus(claimStatusReq.getStatus());
        // TODO: 2022/8/31 剩下的保险金额将原路返回
        return R.status(claimMapper.updateById(claimEntity)>0);
    }

    @Override
    public R upload(List<Long> ids) {
        List<ClaimEntity> claimEntities = this.claimMapper.selectBatchIds(ids);
        String PATH ="C:\\Users\\Administrator\\Desktop\\"+"demo.xlsx";
        EasyExcel.write(PATH,ClaimEntity.class).sheet("模块1").doWrite(claimEntities);
        return R.success("导出成功");
    }

    @Override
    public R uploadBlog(MultipartFile file) {
        String ossUrl = "";
        String filename = file.getOriginalFilename();
        if (file.getSize()> IMAGE_MAX_SIZE){
            return R.fail("文件大小不能超过"+IMAGE_SIZE+"MB!");
        }
        try {
            if (file != null) {
                if (!"".equals(filename.trim())) {
                    File newFile = new File(filename);
                    FileOutputStream os = new FileOutputStream(newFile);
                    os.write(file.getBytes());
                    os.close();
                    // 上传到OSS
                    ossUrl = aliyunOSSUtil.upLoad(newFile);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return R.fail("上传失败");
        }
        return R.data(ossUrl);
    }

    @Override
    public R delete(List<ClaimEntity> claimEntities) {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < claimEntities.size(); i++) {
        ids.add(claimEntities.get(i).getId());
        }
        return R.status(this.claimMapper.deleteBatchIds(ids)>0);
    }
}

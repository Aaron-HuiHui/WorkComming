package com.iwantjob.job.service;

import com.iwantjob.job.dto.CompanyUpdateDTO;
import com.iwantjob.job.dto.CompanyVO;

import java.util.List;

/**
 * 企业信息服务接口
 */
public interface CompanyService {

    /** 企业列表（可按行业过滤，含在招职位数） */
    List<CompanyVO> listCompanies(String industry);

    /** 企业详情（含在招职位数） */
    CompanyVO getCompany(Long id);

    /** HR 认领并编辑企业主页（须发布过该企业职位，或管理员） */
    void updateCompany(Long userId, Integer role, Long companyId, CompanyUpdateDTO dto);
}
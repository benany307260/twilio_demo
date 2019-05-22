package com.bentest.spiders.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.bentest.spiders.entity.AmzCmdtask;
@Repository
public interface AmzCmdtaskRespository extends JpaRepository<AmzCmdtask, Integer>, JpaSpecificationExecutor<AmzCmdtask> {
	
}

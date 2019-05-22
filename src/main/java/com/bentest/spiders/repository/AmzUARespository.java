package com.bentest.spiders.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.bentest.spiders.entity.AmzUA;
@Repository
public interface AmzUARespository extends JpaRepository<AmzUA, Integer>, JpaSpecificationExecutor<AmzUA> {
	
}

package com.bentest.spiders.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.bentest.spiders.entity.AmzProduct;
@Repository
public interface AmzProductRespository extends JpaRepository<AmzProduct, Long>, JpaSpecificationExecutor<AmzProduct> {
	
}

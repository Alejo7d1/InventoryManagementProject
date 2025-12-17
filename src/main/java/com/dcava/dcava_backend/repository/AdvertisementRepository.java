package com.dcava.dcava_backend.repository;

import com.dcava.dcava_backend.model.Advertisement;
import com.dcava.dcava_backend.model.Advertisement.AdType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Integer> {

    List<Advertisement> findByAdType(AdType adType);

    List<Advertisement> findAllByOrderByCreatedAtDesc();
}
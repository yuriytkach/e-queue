package com.yuriytkach.demo.e_queue.repository;

import com.yuriytkach.demo.e_queue.entity.BookingSlot;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingSlotRepository extends CrudRepository<BookingSlot, String> {

}

package com.yuriytkach.demo.e_queue;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingSlotRepository extends CrudRepository<BookingSlot, String> {

}

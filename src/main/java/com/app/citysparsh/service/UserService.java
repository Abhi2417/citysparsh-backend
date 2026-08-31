package com.app.citysparsh.service;

import com.app.citysparsh.dto.CreateOfficerDto;
import com.app.citysparsh.model.User;

import java.util.List;

public interface UserService {

    User createOfficer(CreateOfficerDto dto);
    List<User> getAllCitizens();
    List<User> getAllOfficers();
}

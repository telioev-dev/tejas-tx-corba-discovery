package com.teliolabs.corba.data.repository;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RouteRepository {

    private static final RouteRepository INSTANCE = new RouteRepository();

    public static RouteRepository getInstance() {
        return INSTANCE;
    }
}
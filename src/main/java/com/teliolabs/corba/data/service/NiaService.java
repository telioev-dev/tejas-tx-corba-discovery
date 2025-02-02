package com.teliolabs.corba.data.service;

import com.teliolabs.corba.data.repository.NiaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;

@Log4j2
@RequiredArgsConstructor
public class NiaService {

    private static NiaService instance;
    private final NiaRepository niaRepository;

    // Public method to get the singleton instance
    public static NiaService getInstance(NiaRepository niaRepository) {
        if (instance == null) {
            synchronized (NiaService.class) {
                if (instance == null) {
                    instance = new NiaService(niaRepository);
                    log.info("NiaService instance created.");
                }
            }
        }
        return instance;
    }

    public void publishNiaView(String viewName) throws SQLException {
        niaRepository.executeViewFromFile("sqls/nia/sdh_nia_mv.sql", viewName);
    }
}

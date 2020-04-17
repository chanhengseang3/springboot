package com.construction.persistence.filter;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class FilterConfigurer {

    @Autowired
    private EntityManager entityManager;

    public void enableFilters(final String username) {
        final var session = entityManager.unwrap(Session.class);
        enableUserFilter(session, username);
        enableAdminFilter(session);
    }

    private void enableUserFilter(final Session session, final String username) {
        session.enableFilter("userFilter").setParameter("username", username);
    }

    private void enableAdminFilter(final Session session) {
        session.enableFilter("adminFilter");
    }
}

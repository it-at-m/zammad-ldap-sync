package de.muenchen.zammad.ldap.sync;

import java.util.concurrent.atomic.AtomicLong;

import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Statistic {

    private long ouSize;

    private long userSize;

    @Getter
    private AtomicLong currentOuCount;

    @Getter
    private AtomicLong currentUserCount;

    public Statistic() {
        this.currentOuCount = new AtomicLong(0);
        this.currentUserCount = new AtomicLong(0);
    }

    public void logInfoStartProcessing(LdapOuNode root) {
        this.ouSize = root.flatListLdapOuDTO().size();
        this.userSize = root.flatListLdapUserDTO().size();
        log.info(String.format("Start processing '%o' ldap ou with '%o' user.", ouSize, userSize));
    }

    public void logInfoProcessStatusOuAndUser() {
        log.info(String.format("Processed ou %o/%o. Processed user %o/%o", getCurrentOuCount().get(),
                this.ouSize, getCurrentUserCount().get(), this.userSize));
    }



}

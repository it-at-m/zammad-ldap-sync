package de.muenchen.zammad.ldap.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import de.muenchen.zammad.ldap.tree.LdapOuNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Validation {

    @Value(value = "${sync.message.from}")
    private String from = "noreply@test.com";

    @Value(value = "${sync.message.to}")
    private String to = "test@test.com";

    @Value(value = "${sync.message.subject}")
    private String subject = "Ooops ... test error occurred.";

    private final JavaMailSender javaMailSender;

    void checkOuBases(List<String> ldapSyncDistinguishedNames, Map<String, LdapOuNode> ldapShadetrees) {

        if (ldapShadetrees.size() != ldapSyncDistinguishedNames.size()) {

            var trees = Arrays.asList(ldapShadetrees.keySet().toArray());
            var differences = ldapSyncDistinguishedNames.stream().filter(element -> !trees.contains(element)).toList();

            var sb = new StringBuilder();
            sb.append(System.lineSeparator()).append(System.lineSeparator());
            sb.append(" !!!  No ldap nodes for all ouBases found. Please check the ouBase(s) (ldap distinguished name) availability. Maybe part of a distinguished name was renamed in ldap :");
            sb.append(System.lineSeparator());
            differences.forEach(dn -> {
                sb.append(" !!!    - ");
                sb.append(dn);
                sb.append(System.lineSeparator());
            });

            log.error(sb.toString());

            var message = new SimpleMailMessage();
            message.setFrom(from);
            message.setSubject(subject);
            message.setTo(to);
            message.setText(sb.toString());

            javaMailSender.send(message);

        }

    }
}
package de.muenchen.zammad.ldap.branch;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;

import de.muenchen.zammad.domain.Signatures;
import de.muenchen.zammad.property.OrganizationalUnitsCommonProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class SignatureCache {

    private ZammadService zammadService;
    private OrganizationalUnitsCommonProperties commonProperties;

    private final HashMap<String, Integer> cache = new HashMap<>();

    public Integer findEmailSignatureId(String signatureName) {

        if (signatureName == null) {
            log.warn("Find signatureName started with invalid identifier 'null'.");
            return null;
        }

        if (cache.containsKey(signatureName.toUpperCase())) {
            final var signature = cache.get(signatureName.toUpperCase());
            log.debug("Fetch signature from cache : {}={}", signatureName, signature);
            return signature;
        } else {
            cache.put(signatureName.toUpperCase(), findSignatureId(zammadService.getZammadEmailSignatures(), signatureName,
                    commonProperties.getSignatureStartsWith()));
            log.debug("EmailSignatureId account found in Zammad '{}={}' and added to cache.", signatureName,
                    cache.get(signatureName));
            return cache.get(signatureName);
        }
    }

    private Integer findSignatureId(List<Signatures> signatures, String name, String defaultName) {

        var signature = signatures.stream()
                .filter(signat -> signat.getName().toLowerCase().startsWith(name.toLowerCase())).findFirst()
                .orElse(null);

        if (signature == null && defaultName != null)
            signature = signatures.stream()
                    .filter(signat -> signat.getName().toLowerCase().startsWith(defaultName.toLowerCase())).findFirst()
                    .orElse(null);

        return signature == null ? null : signature.getId();

    }

}

// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetDomainInfoRequest;
import com.zimbra.soap.admin.message.GetDomainInfoResponse;
import com.zimbra.soap.admin.type.DomainSelector;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class GetDomainInfoBrandingTest extends MailboxTestSuite {

    @Test
    void anonymousRequestReceivesBlue3Defaults() throws Exception {
        Domain domain = domain(Map.of());
        Map<String, String> attributes = attributes(domain.getName(), true, null);

        assertEquals("Blue3", attributes.get(ZAttrProvisioning.A_carbonioWebUiTitle));
        assertEquals("Blue3", attributes.get(ZAttrProvisioning.A_carbonioAdminUiTitle));
        assertEquals("#0071bc", attributes.get(ZAttrProvisioning.A_carbonioWebUiPrimaryColor));
        assertEquals("#29abe2", attributes.get(ZAttrProvisioning.A_carbonioWebUiDarkPrimaryColor));
        assertEquals(asset("blue3-marca.png"),
            attributes.get(ZAttrProvisioning.A_carbonioWebUiLoginLogo));
        assertEquals(asset("blue3-marca-branca.png"),
            attributes.get(ZAttrProvisioning.A_carbonioWebUiDarkLoginLogo));
        assertEquals(asset("blue3-simbolo.png"),
            attributes.get(ZAttrProvisioning.A_carbonioWebUiFavicon));
    }

    @Test
    void explicitDomainBrandingOverridesDefaults() throws Exception {
        Map<String, Object> custom = Map.of(
            ZAttrProvisioning.A_carbonioWebUiTitle, "Custom domain",
            ZAttrProvisioning.A_carbonioWebUiLoginLogo, "https://example.com/logo.png",
            ZAttrProvisioning.A_carbonioWebUiPrimaryColor, "#123456");
        Domain domain = domain(custom);
        Map<String, String> attributes = attributes(domain.getName(), true, null);

        custom.forEach((key, value) -> assertEquals(value, attributes.get(key)));
    }

    @Test
    void explicitlySelectedVendorTitleIsPreserved() throws Exception {
        Domain domain = domain(Map.of(ZAttrProvisioning.A_carbonioWebUiTitle, "Carbonio Client"));

        assertEquals("Carbonio Client", attributes(domain.getName(), true, null)
            .get(ZAttrProvisioning.A_carbonioWebUiTitle));
    }

    @Test
    void globalBrandingOverridesForkDefaults() throws Exception {
        Provisioning provisioning = Provisioning.getInstance();
        var config = provisioning.getConfig();
        String name = ZAttrProvisioning.A_carbonioWebUiTitle;
        String previous = config.getAttr(name, false);
        try {
            provisioning.modifyAttrs(config, new HashMap<>(Map.of(name, "Global custom title")));
            Domain domain = domain(Map.of());

            assertEquals("Global custom title", attributes(domain.getName(), true, null).get(name));
        } finally {
            provisioning.modifyAttrs(config, new HashMap<>(Map.of(name, previous == null ? "" : previous)));
        }
    }

    @Test
    void authenticatedRequestReceivesDefaultsWhenRequested() throws Exception {
        Domain domain = domain(Map.of());
        Account account = createAccount().withDomain(domain.getName()).create();

        assertEquals("Blue3", attributes(domain.getName(), true, account)
            .get(ZAttrProvisioning.A_carbonioWebUiTitle));
    }

    @Test
    void rawAuthenticatedRequestDoesNotIncludeForkDefaults() throws Exception {
        Domain domain = domain(Map.of());
        Account account = createAccount().withDomain(domain.getName()).create();

        assertFalse(attributes(domain.getName(), false, account)
            .containsKey(ZAttrProvisioning.A_carbonioWebUiTitle));
    }

    @Test
    void anonymousUnknownDomainReceivesGlobalBrandingDefaults() throws Exception {
        assertEquals("Blue3", attributes(UUID.randomUUID() + ".example.com", true, null)
            .get(ZAttrProvisioning.A_carbonioWebUiTitle));
    }

    private Domain domain(Map<String, Object> attributes) throws Exception {
        return Provisioning.getInstance().createDomain(UUID.randomUUID() + ".example.com",
            new HashMap<>(attributes));
    }

    private Map<String, String> attributes(String domain, boolean applyConfig, Account account)
        throws Exception {
        GetDomainInfo handler = new GetDomainInfo();
        Map<String, Object> context = new HashMap<>();
        ZimbraSoapContext soap = account == null
            ? new ZimbraSoapContext(null, AdminConstants.GET_DOMAIN_INFO_REQUEST, handler,
                context, SoapProtocol.Soap12)
            : new ZimbraSoapContext(AuthProvider.getAuthToken(account), account.getId(),
                SoapProtocol.Soap12, SoapProtocol.Soap12);
        context.put(SoapEngine.ZIMBRA_CONTEXT, soap);
        Element request = JaxbUtil.jaxbToElement(new GetDomainInfoRequest(
            DomainSelector.fromName(domain), applyConfig));
        GetDomainInfoResponse response = JaxbUtil.elementToJaxb(handler.handle(request, context));
        return response.getDomain().getAttrList().stream()
            .collect(Collectors.toMap(attr -> attr.getKey(), attr -> attr.getValue(),
                (first, other) -> first));
    }

    private String asset(String name) {
        return "https://raw.githubusercontent.com/samirhvbr/carbonio-mailbox/main/branding/blue3/" + name;
    }
}

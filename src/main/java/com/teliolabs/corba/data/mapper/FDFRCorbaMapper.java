package com.teliolabs.corba.data.mapper;

import com.teliolabs.corba.data.dto.FDFR;
import com.teliolabs.corba.utils.FDFRUtils;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.flowDomain.FlowDomain_T;
import org.tmforum.mtnm.flowDomainFragment.FlowDomainFragment_T;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
public class FDFRCorbaMapper implements CorbaMapper<FlowDomainFragment_T, FDFR> {

    private static final FDFRCorbaMapper INSTANCE = new FDFRCorbaMapper();

    private final Pattern pattern = Pattern.compile("/mpls=(\\d+):1");

    // Private constructor to enforce Singleton
    private FDFRCorbaMapper() {
    }

    // Public method to get the instance
    public static FDFRCorbaMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public FDFR mapFromCorba(FlowDomainFragment_T input) {
        logFlowDomainFragmentDetails(input);
        String fdfrType = FDFRUtils.getFDFRType(input);
        String fdfrState = String.valueOf(input.fdfrState.value());
        String fDFrRate = String.valueOf(input.transmissionParams.layer);
        String fDFrDir = String.valueOf(input.direction.value());

        String customer = FDFRUtils.getCustomer(input.additionalInfo);
        String bscCir = FDFRUtils.getBscCir(input.additionalInfo);
        String bscEir = FDFRUtils.getBscEir(input.additionalInfo);
        String l2VPNId = FDFRUtils.getL2VPNId(input.additionalInfo);
        String serviceName = FDFRUtils.getServiceName(input.additionalInfo);
        String tunnelIdList = FDFRUtils.getTunnelIdList(input.additionalInfo);
        String vfibProvSize = FDFRUtils.getVFIBProvSize(input.additionalInfo);
        String trafficEnabled = FDFRUtils.getTrafficEnabled(input.additionalInfo);
        String bscProfile = FDFRUtils.getBscProfile(input.additionalInfo);

        log.info("tunnelIdList: {}", tunnelIdList);
        if (tunnelIdList.contains("/mpls=")) {
            Matcher matcher = pattern.matcher(tunnelIdList);
            StringBuilder result = new StringBuilder();
            while (matcher.find()) {
                if (result.length() > 0) {
                    result.append(",");
                }
                result.append("/mpls=1:" + matcher.group(1));
            }
            tunnelIdList = result.toString();
        } else {
            tunnelIdList = "";
        }
        log.info("tunnelIdList now: {}", tunnelIdList);
        return null;
    }

    private void logFlowDomainFragmentDetails(FlowDomainFragment_T flowDomainFragmentT) {
        Arrays.stream(flowDomainFragmentT.name).forEach(attr ->
                log.info("flowDomainFragment Attribute - Name: {}, Value: {}", attr.name, attr.value));
        Arrays.stream(flowDomainFragmentT.additionalInfo).forEach(attr ->
                log.info("FlowDomainFragment Additional Info - Name: {}, Value: {}", attr.name, attr.value));
    }

    @Override
    public Map<String, FDFR> toMap(List<FDFR> list) {
        return null;
    }

    @Override
    public List<FDFR> mapFromCorbaList(List<FlowDomainFragment_T> list) {
        return null;
    }

    public List<FDFR> mapFromCorbaArray(FlowDomainFragment_T[] elementTs) {
        return Arrays.stream(elementTs).map(this::mapFromCorba).collect(Collectors.toList());
    }
}

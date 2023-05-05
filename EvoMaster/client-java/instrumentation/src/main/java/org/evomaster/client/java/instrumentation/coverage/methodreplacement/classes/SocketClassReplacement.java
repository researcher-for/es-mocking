package org.evomaster.client.java.instrumentation.coverage.methodreplacement.classes;

import org.evomaster.client.java.instrumentation.ExternalServiceInfo;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.ExternalServiceInfoUtils;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.MethodReplacementClass;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.Replacement;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.UsageFilter;
import org.evomaster.client.java.instrumentation.shared.ExternalServiceSharedUtils;
import org.evomaster.client.java.instrumentation.shared.ReplacementCategory;
import org.evomaster.client.java.instrumentation.shared.ReplacementType;
import org.evomaster.client.java.instrumentation.staticstate.ExecutionTracer;
import org.evomaster.client.java.utils.SimpleLogger;

import java.io.IOException;
import java.net.*;

import static org.evomaster.client.java.instrumentation.coverage.methodreplacement.ExternalServiceInfoUtils.collectExternalServiceInfo;

public class SocketClassReplacement implements MethodReplacementClass {
    @Override
    public Class<?> getTargetClass() {
        return Socket.class;
    }

    @Replacement(
            type = ReplacementType.TRACKER,
            category = ReplacementCategory.NET,
            replacingStatic = false,
            usageFilter = UsageFilter.ANY
    )
    public static void connect(Socket caller, SocketAddress endpoint, int timeout) throws IOException {
        if (endpoint instanceof InetSocketAddress) {
            InetSocketAddress socketAddress = (InetSocketAddress) endpoint;

            if (ExternalServiceInfoUtils.skipHostnameOrIp(socketAddress.getHostName())
                    || ExecutionTracer.skipHostnameAndPort(socketAddress.getHostName(), socketAddress.getPort())
            ) {
                caller.connect(endpoint, timeout);
                return;
            }

            if (socketAddress.getAddress() instanceof Inet4Address) {
                /*
                    Socket information will be replaced if there is a mapping available for the given address.
                    Inet replacement will pass down the local IP address to Socket instead of the remote host name.
                    Which will create another entry to mock. This will work, but created test case will have
                    local IP address as the DNS record. Actual remote hostname will be lost since Socket will keep
                    passing the replaced IP information to core with port. To avoid this, a reverse lookup is done
                    and if there is a mapping available then Socket will use that value to connect. Otherwise,
                    nothing will happen.
                 */
                if (ExecutionTracer.hasLocalAddressReplacement(socketAddress.getHostName())) {
                    String newHostname = ExecutionTracer.getRemoteHostname(socketAddress.getHostName());
                    ExternalServiceInfo remoteHostInfo = new ExternalServiceInfo(
                            ExternalServiceSharedUtils.DEFAULT_SOCKET_CONNECT_PROTOCOL,
                            newHostname,
                            socketAddress.getPort()
                    );
                    String[] ipAndPort = collectExternalServiceInfo(remoteHostInfo, socketAddress.getPort());

                    InetSocketAddress replaced = new InetSocketAddress(InetAddress.getByName(ipAndPort[0]), Integer.parseInt(ipAndPort[1]));
                    caller.connect(replaced, timeout);
                    return;
                }
            }
        }
        SimpleLogger.warn("not handle the type of endpoint yet:" + endpoint.getClass().getName());
        caller.connect(endpoint, timeout);
    }
}

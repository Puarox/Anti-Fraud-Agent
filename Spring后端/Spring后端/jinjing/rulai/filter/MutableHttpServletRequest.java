package rulai.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

/**
 * <p>
 * 可修改请求头的请求包装类
 * </p>
 *
 * @author Mistry
 * @since 2025-1-6
 */

public class MutableHttpServletRequest extends HttpServletRequestWrapper {
    
    private final Map<String, String> customHeaders;
    
    public MutableHttpServletRequest(HttpServletRequest request) {
        super(request);
        this.customHeaders = new HashMap<>();
    }
    
    /**
     * 添加自定义请求头
     */
    public void putHeader(String name, String value) {
        this.customHeaders.put(name.toLowerCase(), value);
    }
    
    @Override
    public String getHeader(String name) {
        String headerValue = customHeaders.get(name.toLowerCase());
        if (headerValue != null) {
            return headerValue;
        }
        return super.getHeader(name);
    }
    
    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> set = new HashSet<>(customHeaders.keySet());
        
        Enumeration<String> e = super.getHeaderNames();
        while (e.hasMoreElements()) {
            String n = e.nextElement();
            set.add(n.toLowerCase());
        }
        
        return Collections.enumeration(set);
    }
    
    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = Collections.list(super.getHeaders(name));
        String customValue = customHeaders.get(name.toLowerCase());
        
        if (customValue != null && !values.contains(customValue)) {
            values.add(customValue);
        }
        
        return Collections.enumeration(values);
    }
}
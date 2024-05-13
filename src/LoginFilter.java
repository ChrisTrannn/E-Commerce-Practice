import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // redirect _dashboard to employee login page
        if (isEmployeeLoginEntryPoint(httpRequest.getRequestURI())) {
            if (httpRequest.getSession().getAttribute("user") != null) {
                if (httpRequest.getSession().getAttribute("isEmployee") != null) {
                    chain.doFilter(request, response);
                    return;
                } else {
                    httpResponse.sendRedirect("/cs122b-s24-team-sc/main-page.html");
                }
            } else {
                httpResponse.sendRedirect("/cs122b-s24-team-sc/_dashboard/login-page.html");
            }
            return;
        }

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {
            httpResponse.sendRedirect("/cs122b-s24-team-sc/login-page.html");
        } else {
            // isEmployee attribute is set, user can access employee dashboard
            // if isEmployee attribute is not set, redirect to main-page.html
            if (httpRequest.getSession().getAttribute("isEmployee") != null) {
                chain.doFilter(request, response);
            } else {
                if (httpRequest.getRequestURI().contains("/_dashboard")) {
                    httpResponse.sendRedirect("/cs122b-s24-team-sc/main-page.html");
                } else {
                    chain.doFilter(request, response);
                }
            }
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    // Check if the requested URL is the entry point for employee login
    private boolean isEmployeeLoginEntryPoint(String requestURI) {
        return requestURI.equals("/cs122b-s24-team-sc/_dashboard");
    }

    public void init(FilterConfig fConfig) {
        // allowed urls for users
        allowedURIs.add("login-page.html");
        allowedURIs.add("login-page.js");
        allowedURIs.add("login-page.css");
        allowedURIs.add("api/login");

        // allowed urls for employees
        allowedURIs.add("/_dashboard/login-page.html");
        allowedURIs.add("/_dashboard/login-page.js");
        allowedURIs.add("/_dashboard/login-page.css");
        allowedURIs.add("/_dashboard/api/employee-login");
    }

    public void destroy() {
        // ignored.
    }
}

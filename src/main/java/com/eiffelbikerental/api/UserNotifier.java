package com.eiffelbikerental.api;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.eiffelbikerental.service.WaitingListService;

@WebServlet("/notifyUser")
public class UserNotifier extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private WaitingListService waitingListService = new WaitingListService(); // Create instance

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int bikeId = Integer.parseInt(request.getParameter("bikeId")); // Pass bikeId as a query parameter
        
        // Call the service method
        waitingListService.notifyFirstUserWhenBikeIsAvailable(bikeId);

        // Respond to the client
        response.getWriter().println("Notification logic executed for bike ID: " + bikeId);
    }
}


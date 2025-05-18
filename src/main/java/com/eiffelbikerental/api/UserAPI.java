package com.eiffelbikerental.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.eiffelbikerental.model.ChangePasswordRequest;
import com.eiffelbikerental.model.User;
import com.eiffelbikerental.service.UserService;

@Path("/users")
public class UserAPI {

	private UserService userService = new UserService();

	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerUser(User user) {
		// Validate email format based on user type
		if (user.getUserType() != null) {
			// Compare with User.UserType enum values
			switch (user.getUserType()) {
			case STUDENT:
			case EMPLOYEE:
				if (!user.getEmail().endsWith("@univ-eiffel.fr")) {
					return Response.status(Response.Status.BAD_REQUEST).entity(
							"{\"message\": \"Invalid email address. Only Gustave Eiffel University emails are allowed for students and employees.\"}")
							.build();
				}
				break;
			case CUSTOMER:
				if (!user.getEmail().endsWith("@gmail.com")) {
					return Response.status(Response.Status.BAD_REQUEST).entity(
							"{\"message\": \"Invalid email address. Only Gmail addresses are allowed for customers.\"}")
							.build();
				}
				break;
			default:
				return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Invalid user type.\"}")
						.build();
			}
		}

		// Validate password length
		if (user.getPassword() == null || user.getPassword().length() < 8) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"message\": \"Weak password. Password must be at least 8 characters long.\"}").build();
		}

		// Proceed with registration logic
		boolean registered = userService.registerUser(user.getUsername(), user.getEmail(), user.getPassword(),
				user.getUserType());

		if (registered) {
			return Response.status(Response.Status.CREATED).entity("{\"message\": \"User registered successfully\"}")
					.build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR) // Changed to INTERNAL_SERVER_ERROR for
																			// failures that may not be related to the
																			// client request
					.entity("{\"message\": \"Registration failed. Please try again later.\"}").build();
		}
	}

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response loginUser(User user) {
		String username = user.getUsername();
		String password = user.getPassword();

		// Check login credentials
		if (userService.loginUser(username, password)) {
			// Fetch the user ID and user type after successful login
			int userId = userService.getUserIdByUsername(username);
			String userType = userService.getUserTypeByUsername(username); // Assuming this method exists in your
																			// service

			// Return the user ID and user type in the response
			String responseJson = String
					.format("{\"message\":\"Login successful\", \"userId\":%d, \"userType\":\"%s\"}", userId, userType);
			return Response.ok().entity(responseJson).build();
		} else {
			return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\":\"Invalid credentials\"}")
					.build();
		}
	}

	@POST
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	public Response logoutUser(@QueryParam("userId") String userId) {
		if (userId == null || userId.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity("User ID is required").build();
		}

		// No session or token invalidation needed on the backend
		return Response.ok("Logout successful. Please clear your client-side session data.").build();
	}

	@GET
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserById(@PathParam("userId") int userId) {
		User user = userService.getUserById(userId);
		if (user != null) {
			return Response.ok(user).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).entity("{\"message\":\"User not found\"}").build();
		}
	}

	@PUT
	@Path("/{userId}/password")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response changePassword(@PathParam("userId") int userId, ChangePasswordRequest request) {
		// Extract currentPassword, newPassword, and confirmPassword from the request
		String currentPassword = request.getCurrentPassword();
		String newPassword = request.getNewPassword();
		String confirmPassword = request.getConfirmPassword();

		// Call the service method to change the password
		boolean success = userService.changePassword(userId, currentPassword, newPassword, confirmPassword);

		if (success) {
			return Response.ok().entity("{\"message\": \"Password changed successfully.\"}").build();
		} else {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"message\": \"Failed to change password. Please check your inputs.\"}").build();
		}
	}

	@PUT
	@Path("/{userId}/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUser(@PathParam("userId") int userId, User userUpdateInfo) {
		if (userId <= 0 || userUpdateInfo == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("{\"message\":\"Invalid user ID or request body\"}").build();
		}

		boolean updated = userService.updateUserById(userId, userUpdateInfo.getUsername(), userUpdateInfo.getEmail(),
				userUpdateInfo.getPassword(), userUpdateInfo.getUserType(), userUpdateInfo.getPhone(),
				userUpdateInfo.getAddress(), userUpdateInfo.getCountry());

		if (updated) {
			return Response.ok("{\"message\":\"User updated successfully\"}").build();
		} else {
			return Response.status(Response.Status.NOT_FOUND)
					.entity("{\"message\":\"User not found or update failed\"}").build();
		}
	}

	@Path("/{userId}/rentals")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserRentals(@PathParam("userId") int userId) {

		int rentalsCount = userService.getRentalsCount(userId);
		return Response.ok("{\"rentalsCount\": " + rentalsCount + "}").build();
	}


	@Path("/{userId}/waitinglist")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserwaiitnlist(@PathParam("userId") int userId) {

		int waitinglistCount = userService.getWaitinglistCount(userId);
		return Response.ok("{\"waitinglistCount\": " + waitinglistCount + "}").build();
	}
	
	
}

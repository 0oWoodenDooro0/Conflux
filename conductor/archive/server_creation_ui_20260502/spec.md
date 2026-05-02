# Specification: Server Creation UI and Owner Support

**Overview**
The current application lacks a user-accessible way to create new servers. While a `CreateServerScreen` exists, it is not linked from the main application interface. Furthermore, the server creation API currently hardcodes the server owner to "default-user" instead of using the actual logged-in user's information. This track will implement the necessary UI and API changes to allow users to create servers associated with their account.

**Functional Requirements**
1. **Server Creation Button**: Add a button to the bottom of the server list sidebar. This button should be easily identifiable (e.g., a "+" icon).
2. **Navigation to Create Server**: Clicking the button should navigate the user to the `CreateServerScreen`.
3. **API Update (Shared/Client)**:
   - Update `CreateServerRequest` model to include an `ownerId` field (using username as the key for now).
   - Update `ServerApiClient.createServer` to accept and send the `ownerId`.
4. **API Update (Server)**:
   - Update the `/api/servers` POST route to extract the `ownerId` from the request and use it when creating the `Server` object in the database.
   - Update the `/api/servers` GET route (via `getServersForUser`) to robustly handle the `userId` parameter, supporting both UUIDs and usernames, and ensuring it returns servers where the user is either the owner or a member.
5. **UI Integration**: Ensure the `MainScreen` or `ServerSidebar` can handle navigation to the server creation flow.

**Acceptance Criteria**
- A "+" button is visible at the bottom of the server sidebar.
- Clicking the button opens the server creation interface.
- Creating a server via this interface correctly associates the new server with the logged-in user (username).
- The new server appears in the user's server list after creation.

**Out of Scope**
- Advanced server settings (roles, permissions) beyond the initial name.
- Server icon upload (only URL for now).
- Full authentication system (continuing to use username as the identifier).

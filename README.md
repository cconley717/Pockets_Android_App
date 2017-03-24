# Pockets_Android_App

Pockets is a social app that revolves around the accumulation and expendature of internet points. For example, web sites such as Reddit have the karma system, but these points aren't really used for anything. In Pockets, users create chat rooms (pockets) on a Google Map. These "pockets" accumulate lint which can then be used for creating bigger pockets. The bigger the pocket, the more lint it will require.

Pockets makes use of OKHttp when communicating with the back-end over HTTP and socket.io for chat functionality over websocket. User accounts and chat rooms are stored within a PostgreSQL database and MongoDB is used for storing user sessions.

# assignment
This java program accets three input parameters,

Playlist file in JSON format, which has list of users, playlist and songs. Each playlist is create for a user with set of songs. See the sample playlist.json file here - https://github.com/chandrareddyp/assignment/blob/master/properties/playlist.json
changes.json file, which has "update_playlist" to update the existing play list in playlist.json, and "remove_playlist" which has user name for which playlist's need to remove from playlist.json file. Sample changes.json file - https://github.com/chandrareddyp/assignment/blob/master/properties/changes.json
output file path, which is produce after applying changes.json on playlist.json file. Sample output file - https://github.com/chandrareddyp/assignment/blob/master/properties/output.json
How to run the program: This program needs json-simple-1.1.jar file, so make sure its added in classpath. You can check-out the code and add it as a project into eclipse or any ide and run by providing run time parameters. otherwise, check-out the code, then go to bin folder, then run the program as below: java com.highspot.PlaylistProcess playlist.json changes.json output.json

How to scale to handle large input files and/or very large changes files:

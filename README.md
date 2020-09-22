
This java program accepts three input parameters:-

1.Playlist file in JSON format, which has list of users, playlist and songs. Each playlist is create for a user with set of songs. See the sample playlist.json file here - https://github.com/chandrareddyp/assignment/blob/master/properties/playlist.json
2.changes.json file, which has "update_playlist" to update the existing play list in playlist.json, and "remove_playlist" which has user name for which playlist's need to remove from playlist.json file. Sample changes.json file - https://github.com/chandrareddyp/assignment/blob/master/properties/changes.json
3.output file path, which is produce after applying changes.json on playlist.json file. Sample output file - https://github.com/chandrareddyp/assignment/blob/master/properties/output.json
How to run the program: This program needs json-simple-1.1.jar file, so make sure its added in classpath. You can check-out the code and add it as a project into eclipse or any ide and run by providing run time parameters. otherwise, check-out the code, then go to bin folder, then run the program as below: java com.highspot.PlaylistProcess playlist.json changes.json output.json

Note:: the playlist.json and changes.json file formats should be proper as shown the above links otherwise program does not work.

How to scale to handle large input files and/or very large changes files:
Input files very large:
If very large input file, then process chunk of input file instead of loading full file into memory, after processing block of the file store info as below:
1. Users data, store user's data sorted by user id, store data into n number of file, fix the file size and range of user id's data its stored. So when we are searching for user data by id then we can search using binary search, we should be able to search in O(log n). 
Create kind of index on user name, means store user names in sorted order and its ids. if we want to search a user id by user name then we should be able to get in O(log n) time.
2. Songs data, similar to user data, store songs data also in smaller files, sort by song id's (id, artist and title). Maintain each file metadata (which has plays id's range its storing, its kind of consistent hashing technique).
Create a index for artist which means file will have artist (sort by) and id of song (artist, id). 
Create a index for title which means file will have title (sort by) and id of song (title, id).
In this way we can search song by id or by title or by artist at O(long n) time.
Overall songs data will be stored in 3 sets of files, first set of files stores data by songs ids, second set of files stores songs by artist and third set of files stores songs by title.
3. Playlist data, similarto song/user data, store playlist data also in smaller files, sort by playlist id (id, userId, songIds). Maintain each file metadata (which has plays id's range its storing, its kind of consistent hashing technique).
Create a index for userId, file stores userId and playlist id. Here also we may need to store info in multuple files and maintain range of userId's stored on each file.
Playlist data stores in two sets of files, first set of files stores playlist data by playlist id, and second set of files stores by userId.
In this way we can search playlist data by playlist id or userId in O(long n) time.

Changes files very large:
If the changes file is very large then we can process chunk/block of the file instead of full file, as we haev already stored the input file info properly and we do have indices created on different attributes, now we can search songs info (by artist or title) and users info (by user name) in O(log n) time, and we can also search playlist info for the give n user in O(log n) time, and we can update existing playlist or add new playlist to existing file (with O(log n) time) which are created in the above input file process.

We can also scale this program in n number of threads or microservices or lambda functions, but we need to use locking (optimistic/pessimistic) or synchronization to make sure no two threads are modifying the same file same time.

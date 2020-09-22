package com.highspot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Purpose: This tool accepts two inputs. 1. existing playlist file: playlist
 * file which has users, songs and playlists info. It should be proper format.
 * 2. update playlist file: changes/update playlist file has new playlists to
 * add to specific user and delete list of playlists.
 * 
 * It processes the first input file and modified first input file data based on
 * second input file. and finally it produces the output same as first input
 * file format after modifications.
 * 
 * @author Chandra Pamuluri
 */
public class PlaylistProcess {

  private Map<String, List<Song>> songsByArtist = new HashMap<>();
  private Map<String, Song> songsByTitle = new HashMap<>();
  private Map<Integer, Song> songsById = new HashMap<>();
  private Map<Integer, Playlist> playlistByUserId = new HashMap<>();
  private Map<Integer, Playlist> playlistById = new HashMap<>();
  private Map<String, Integer> userByName = new HashMap<>();
  private Map<Integer, String> userByID = new HashMap<>();
  private int nextUserId = 1;
  private int nextSongId = 1;
  private int nextPlaylistId = 1;

  public static void main(String[] args) {
    if (args.length != 3) {
      System.out
          .println("Please provide valid number of input parameters. input file, change file and output file path.");
    }
    for (int i = 0; i < 3; i++) {
      System.out.println("Input parameter " + i + ": " + args[i]);
    }
    System.out.println(args[0]);
    PlaylistProcess playlistProcess = new PlaylistProcess();
    playlistProcess.initializeDataFromFile(args[0]);
    playlistProcess.updateData(args[1]);
    playlistProcess.outputToFile(args[2]);
  }

  private void initializeDataFromFile(String file) {
    JSONParser parser = new JSONParser();
    try {
      Object obj = parser.parse(new FileReader(file));
      JSONObject jsonObject = (JSONObject) obj;

      JSONArray songs = (JSONArray) jsonObject.get("songs");
      processSongs(songs);

      JSONArray users = (JSONArray) jsonObject.get("users");
      processUsers(users);

      JSONArray playlist = (JSONArray) jsonObject.get("playlists");
      processPlaylist(playlist);

    } catch (Exception exe) {
      System.out.println(exe.getMessage());
    }
  }

  private void updateData(String file) {
    JSONParser parser = new JSONParser();
    try {

      Object obj = parser.parse(new FileReader(file));
      JSONObject jsonObject = (JSONObject) obj;

      JSONArray playlist = (JSONArray) jsonObject.get("update_playlist");
      updatePlaylist(playlist);

      JSONArray remove = (JSONArray) jsonObject.get("remove_playlist");
      deletePlaylist(remove);

    } catch (Exception exe) {
      new RuntimeException("Input data is not proper:" + exe.toString());
    }
  }

  private void outputToFile(String file) {
    JSONArray usersArray = new JSONArray();
    JSONArray songsArray = new JSONArray();
    JSONArray playlistArray = new JSONArray();
    // Users
    getUpdatedUserInfo(usersArray);
    // songs
    getUpdatedSongsInfo(songsArray);
    // Playlist
    getUpdatedPlaylistInfo(playlistArray);
    // Write to file
    writeToOutputFile(file, usersArray, songsArray, playlistArray);
  }

  @SuppressWarnings("unchecked")
  private void writeToOutputFile(String file, JSONArray usersArray, JSONArray songsArray, JSONArray playlistArray) {
    JSONObject obj = new JSONObject();
    obj.put("users", usersArray);
    obj.put("playlists", playlistArray);
    obj.put("songs", songsArray);
    try (FileWriter fw = new FileWriter(file)) {
      System.out.println("Updating output file...:" + file);
      fw.write(obj.toJSONString());
      fw.flush();
      System.out.println("Done updating output file:" + file);
    } catch (Exception e) {
      System.out.println("Got error while writing output:" + e.toString());
    }
  }

  @SuppressWarnings("unchecked")
  private void getUpdatedPlaylistInfo(JSONArray playlistArray) {
    List<Integer> playList = new ArrayList<>();
    for (Integer key : playlistById.keySet()) {
      playList.add(key);
    }
    Collections.sort(playList);
    for (Integer pid : playList) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("id", pid);
      map.put("user_id", playlistById.get(pid).userId);
      map.put("song_ids", playlistById.get(pid).songs);
      playlistArray.add(map);
    }
  }

  @SuppressWarnings("unchecked")
  private void getUpdatedSongsInfo(JSONArray songsArray) {
    List<Integer> songsList = new ArrayList<>();
    for (Integer key : songsById.keySet()) {
      songsList.add(key);
    }
    Collections.sort(songsList);
    for (Integer sid : songsList) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("id", sid);
      map.put("artist", songsById.get(sid).artist);
      map.put("title", songsById.get(sid).title);
      songsArray.add(map);
    }
  }

  @SuppressWarnings("unchecked")
  private void getUpdatedUserInfo(JSONArray usersArray) {
    List<Integer> userList = new ArrayList<>();
    for (Integer key : userByID.keySet()) {
      userList.add(key);
    }
    Collections.sort(userList);
    for (Integer uid : userList) {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("id", uid);
      map.put("name", userByID.get(uid));
      usersArray.add(map);
    }
  }

  @SuppressWarnings("unchecked")
  private void deletePlaylist(JSONArray remove) {
    if (remove != null) {
      for (Object list : remove) {
        Map<String, Object> map = (Map<String, Object>) list;
        Playlist pl = playlistByUserId.get(userByName.get(map.get("username")));
        if (pl != null) {
          playlistById.remove(pl.id);
          playlistByUserId.remove(pl.userId);
          System.out.println("Playlist with the username:" + userByName.get(map.get("username")) + " is deleted.");
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void updatePlaylist(JSONArray playlist) {
    if (playlist != null) {
      System.out.println("In changes file total playlist to update: " + playlist.size());
      int i = 1;
      for (Object list : playlist) {
        Map<String, Object> map = (Map<String, Object>) list;
        String username = (String) map.get("username");
        int currUserID;
        if (userByName.containsKey(username)) {
          currUserID = userByName.get(username);
          System.out.println(
              "Existing Playlist with username:" + username + ", will update with provided songs in changes file.");
        } else {
          currUserID = nextUserId;
          nextUserId++;
          System.out.println(
              "New Playlist with new username:" + username + ", will update with provided songs in changes file.");
          userByName.put(username, currUserID);
          userByID.put(currUserID, username);
        }

        JSONArray songs = (JSONArray) map.get("songs");
        if (songs != null) {
          System.out.println("In changes file total songs to update for playlist " + (i++) + ": " + songs.size());
          for (Object song : songs) {
            Map<String, String> songMap = (Map<String, String>) song;
            String artist = songMap.get("artist");
            String title = songMap.get("title");
            int songid;
            if (songsByTitle.containsKey(title)) {
              songid = songsByTitle.get(title).id;
            } else {
              songid = nextSongId;
              nextSongId++;
              Song s = new Song(songid, artist, title);
              songsByTitle.put(title, s);
              songsById.put(songid, s);
            }
            if (playlistByUserId.containsKey(currUserID)) {
              playlistByUserId.get(currUserID).songs.add(songid);
            } else {
              int plid = nextPlaylistId;
              nextPlaylistId++;
              Set<Integer> set = new HashSet<>();
              set.add(songid);
              Playlist pl = new Playlist(plid, currUserID, set);
              playlistByUserId.put(currUserID, pl);
              playlistById.put(plid, pl);
            }
          } // for loop
        }
      } // for loop
    }
  }

  private void processPlaylist(JSONArray playlist) {
    if (playlist != null) {
      System.out.println("In input file total playlist: " + playlist.size());
      for (Object list : playlist) {
        Map<String, Object> map = (Map<String, Object>) list;
        String id = (String) map.get("id");
        String userId = (String) map.get("user_id");
        Set<Integer> set = new HashSet<>();
        for (String sid : (List<String>) map.get("song_ids")) {
          Integer in = Integer.parseInt(sid);
          set.add(in);
        }
        if (nextPlaylistId <= Integer.parseInt(id))
          nextPlaylistId = Integer.parseInt(id) + 1;
        Playlist pl = new Playlist(Integer.parseInt(id), Integer.parseInt(userId), set);
        playlistByUserId.put(Integer.parseInt(userId), pl);
        playlistById.put(Integer.parseInt(id), pl);
      }
    }
  }

  private void processUsers(JSONArray users) {
    if (users != null) {
      System.out.println("In input file total users: " + users.size());
      for (Object user : users) {
        Map<String, String> map = (Map<String, String>) user;
        String id = map.get("id");
        String name = map.get("name");
        if (nextUserId <= Integer.parseInt(id))
          nextUserId = Integer.parseInt(id) + 1;
        userByID.put(Integer.parseInt(id), name);
        userByName.put(name, Integer.parseInt(id));
      }
    }
  }

  private void processSongs(JSONArray songs) {
    if (songs != null) {
      System.out.println("In input file total songs: " + songs.size());
      for (Object song : songs) {
        Map<String, String> map = (Map<String, String>) song;
        String id = map.get("id");
        int idInt = Integer.parseInt(id);
        if (nextSongId <= idInt) {
          nextSongId = idInt + 1;
        }
        String artist = map.get("artist");
        String title = map.get("title");
        Song s = new Song(idInt, artist, title);
        songsByTitle.put(title, s);
        List<Song> sa = songsByArtist.getOrDefault(artist, new ArrayList<>());
        songsByArtist.put(artist, sa);
        songsById.put(idInt, s);
      }
    }
  }

  private static String readInput() {
    String name = null;

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));) {
      name = reader.readLine();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return name;
  }

  class Playlist {
    int id;
    int userId;
    Set<Integer> songs;

    Playlist(int id, int userId, Set<Integer> songs) {
      this.id = id;
      this.userId = userId;
      this.songs = songs;
    }
  }

  class Song {
    int id;
    String artist;
    String title;

    Song(int id, String artist, String title) {
      this.id = id;
      this.artist = artist;
      this.title = title;
    }
  }
}

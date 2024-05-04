package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
//    private HashMap<Group, List<User>> groupUserMap;
//    private HashMap<Group, List<Message>> groupMessageMap;
//    private HashMap<Message, User> senderMap;
//    private HashMap<Group, User> adminMap;
//    private HashMap<String,User> userMobile;
//    private int customGroupCount;
//    private int messageId;
//
//    public WhatsappRepository(){
//        this.groupMessageMap = new HashMap<Group, List<Message>>();
//        this.groupUserMap = new HashMap<Group, List<User>>();
//        this.senderMap = new HashMap<Message, User>();
//        this.adminMap = new HashMap<Group, User>();
//        this.userMobile = new HashMap<>();
//        this.customGroupCount = 0;
//        this.messageId = 0;
//    }
    private HashMap<String,String> userMap = new HashMap<>();
    private HashMap<Group, List<User>> groupUserMap = new HashMap<>();
    private HashMap<Group, List<Message>> groupMessageMap = new HashMap<>();
    private HashSet<Message> messageSet = new HashSet<>();
    private int customGroupCount = 1;
    private int messageId = 0;

    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        if(userMap.containsKey(mobile)){
            throw new CustomException("User already exists");
        }else{
            userMap.put(mobile,name);
            return "SUCCESS";
        }
    }

    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.
        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.
        Group grp = null;
        if(users.size()>2){
            grp = new Group("Group "+customGroupCount,users.size());
            customGroupCount++;
            groupUserMap.put(grp,users);
        }else if(users.size() == 2){
            grp = new Group(users.get(1).getName(),2);
            groupUserMap.put(grp,users);
        }
        return grp;
    }

    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        Message m = new Message();
        m.setContent(content);
        messageSet.add(m);
        messageId++;
        return messageId;
    }

    Group getGroupByName(String name){
        for(Group g : groupUserMap.keySet()){
            if(g.getName().equals(name)){
                return g;
            }
        }
        return null;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group

        Group g = new Group();
        for(Group x : groupUserMap.keySet()){
            if(x.getName().equals(group.getName())){
                g = x;

            }
        }
        if(groupUserMap.containsKey(g)){
            boolean flag = false;
            for(User user : groupUserMap.get(getGroupByName(group.getName()))){
                if(user.getName().equals(sender.getName())) {
                    flag = true;
                    break;
                }
            }
            if(flag){
                if(groupMessageMap.containsKey(group)){
                    List<Message> msgs = groupMessageMap.get(group);
                    msgs.add(message);
                    groupMessageMap.put(group,msgs);
                    return groupMessageMap.get(group).size();
                }else{
                    List<Message> msgs= new ArrayList<>();
                    msgs.add(message);
                    groupMessageMap.put(group,msgs);
                    return groupMessageMap.get(group).size();
                }
            }else{
                throw new CustomException("You are not allowed to send message");
            }
        }else{
            throw new CustomException("Group does not exist");
        }
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.

        int c = 0;
        if (groupUserMap.containsKey(getGroupByName(group.getName()))){
            boolean admin = false;
            boolean userExist = false;
            for (User us : groupUserMap.get(getGroupByName(group.getName()))){
                if (us.getName().equals(approver.getName()) && c == 0){
                    admin = true;
                    c++;
                }
                if(us.getName().equals(user.getName())){
                    userExist = true;
                    groupUserMap.get(getGroupByName(group.getName())).remove(us);
                    break;
                }
            }
            if(!admin){
                throw new CustomException("Approver does not have rights");
            }
            if(!userExist){
                throw new CustomException("User is not a participant");
            }

            if(admin && userExist){
                List<User> users = groupUserMap.get(getGroupByName(group.getName()));
                users.add(0,user);
                groupUserMap.put(getGroupByName(group.getName()),users);
                return "SUCCESS";
            }


        }else{
            throw new CustomException("Group does not exist");
        }
        return "";
    }

}

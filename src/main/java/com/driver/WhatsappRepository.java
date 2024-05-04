package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashMap<String,User> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashMap<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }
    public boolean findUserByPhone(String number){

        if(userMobile.containsKey(number)){
            return true;
        }
        else{
            return false;
        }
    }

    public String addUser(String name, String number){
        userMobile.put(number,new User(name,number));
        return "SUCCESS";
    }

    public Group addGroup(List<User>userList){

        int grpsize = userList.size();
        User admin = userList.get(0);
        String grpName = "";
        if(grpsize == 1){
            grpName = userList.get(1).getName();
        }
        else{
            this.customGroupCount += 1;
            grpName = "Group" + customGroupCount;
        }
        Group group=new Group(grpName,grpsize);
        groupUserMap.put(group,userList);
        adminMap.put(group,admin);
        return  group;
    }
    public int createMessage(String content)
    {
        this.messageId+=1;
        Message message=new Message(messageId,content);
        return messageId;
    }
    public int sendMessage(Message message,User sender,Group group) throws Exception
    {
        if(groupUserMap.containsKey(group)==false)
        {
            throw new Exception("Group does not exist");
        }
        List<User> memberList=groupUserMap.get(group);
        if(memberList.contains(sender)==false)
        {
            throw new Exception("You are not allowed to send message");
        }
        List<Message>msgList=groupMessageMap.getOrDefault(group,new ArrayList<>());
        msgList.add(message);
        groupMessageMap.put(group,msgList);
        senderMap.put(message,sender);
        return msgList.size();
    }
    public String  changeAdmin(User approver, User user, Group group) throws Exception
    {
        if(groupUserMap.containsKey(group)==false)
        {
            throw new Exception("Group does not exist");
        }
        if(adminMap.getOrDefault(group,null)==null)
        {
            throw new Exception("Approver does not have rights");
        }
        if(groupUserMap.get(group).contains(user)==false)
        {
            throw new Exception("User is not a participant");
        }
        adminMap.put(group,approver);
        return "SUCCESS";
    }
    public int removeUser(User user) throws Exception
    {
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        Group groupF=null;
        for(Group group:groupUserMap.keySet())
        {
            List<User> userList=groupUserMap.get(group);
            if(userList.contains(user))
            {
                groupF=group;
                break;
            }
        }
        if(groupF==null)
        {
            throw new Exception("User not found");
        }
        if(adminMap.get(groupF)==user)
        {
            throw new Exception("Cannot remove admin");
        }
        groupF.setNumberOfParticipants(groupF.getNumberOfParticipants()-1);
        groupUserMap.get(groupF).remove(user);
        List<Message>msgList=groupMessageMap.get(groupF);
        for(Message msg:senderMap.keySet())
        {
            if(senderMap.get(msg)==user)
            {
                msgList.remove(msg);
                senderMap.remove(msg);
            }
        }
        String phoneNo= user.getMobile();
        this.userMobile.remove(user);
        int OverallMsg=0;
        int msgInGrp=0;
        int numberOfmember=0;
        numberOfmember+=groupUserMap.get(groupF).size();
        msgInGrp+=groupMessageMap.get(groupF).size();
        OverallMsg+=senderMap.size();
        return OverallMsg+msgInGrp+numberOfmember;
    }
    public String findMessage(Date start, Date end, int k) throws Exception {
        //This is a bonus problem and does not contain any marks
        // Find the Kth the latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception

        List<Message> messageList = new ArrayList<>();

        for (Message message : senderMap.keySet()){
            Date time = message.getTimestamp();
            if (start.before(time) && end.after(time)){
                messageList.add(message);
            }
        }
        if (messageList.size() < k){
            throw  new Exception("K is greater than the number of messages");
        }

        Map<Date , Message> hm = new HashMap<>();

        for (Message message : messageList){
            hm.put(message.getTimestamp(),message);
        }
        List<Date> dateList = new ArrayList<>(hm.keySet());

        Collections.sort(dateList, new sortCompare());

        Date date = dateList.get(k-1);
        String ans = hm.get(date).getContent();
        return ans;
    }
    class sortCompare implements Comparator<Date> {
        @Override
        // Method of this class
        public int compare(Date a, Date b)
        {
            /* Returns sorted data in Descending order */
            return b.compareTo(a);
        }
    }

}

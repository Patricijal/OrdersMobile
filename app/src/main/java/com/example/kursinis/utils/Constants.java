package com.example.kursinis.utils;

public class Constants {
    public static final String HOME_URL = "http://192.168.1.151:8080/";
    public static final String VALIDATE_USER_URL = HOME_URL + "validateUser";
    public static final String GET_ALL_RESTAURANTS_URL = HOME_URL + "allRestaurants";
    public static final String CREATE_BASIC_USER_URL = HOME_URL + "insertBasicUser";
    public static final String CREATE_DRIVER_URL = HOME_URL + "insertDriver";

    public static final String GET_USER_BY_ID_URL = HOME_URL + "getUserById/";
    public static final String UPDATE_USER_URL = HOME_URL + "updateUserById/";

    public static final String GET_ORDERS_BY_USER = HOME_URL + "getByUserId/";
    public static final String GET_MESSAGES_BY_ORDER = HOME_URL + "getMessagesForOrder/";
//    public static final String SEND_MESSAGE_IN_CHAT = HOME_URL + "insertMessage/";
    public static final String SEND_MESSAGE = HOME_URL + "sendMessage";

//    public static final String GET_CUISINES_BY_RESTAURANT = HOME_URL + "getAllCuisines";
    public static final String GET_RESTAURANT_MENU = HOME_URL + "getMenuRestaurant/";
    public static final String CREATE_ORDER = HOME_URL + "createOrder";

}

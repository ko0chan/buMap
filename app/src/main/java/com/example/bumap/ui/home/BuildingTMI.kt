package com.example.bumap.ui.home

import com.google.firebase.database.IgnoreExtraProperties
import com.naver.maps.geometry.LatLng

class BuildingTMI {
    //var placename
    //var p = Position()
}
class Position{
    var placename: String =""
    var lat: String = ""
    var lng: String = ""

}

class FloorNumber{
//    lateinit var fn :FloorNumber
    var rn: HashMap<String, RoomNumber> = HashMap()
}

class RoomNumber{
//    lateinit var roomnumber :RoomNumber
    var room: HashMap<String, Room> = HashMap()
}

class Room{
    lateinit var location :Position
    lateinit var name : String
}
package com.example.bumap.ui.home

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.bumap.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.InfoWindow.DefaultTextAdapter
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.home_buliding.*
import kotlinx.android.synthetic.main.home_buliding.view.*
import java.util.*

class BuildingInfo : FragmentActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.home_buliding)

        val fm = supportFragmentManager
        val lat1 = intent.getDoubleExtra("lat", 0.0)
        val lng1 = intent.getDoubleExtra("lng", 0.0)
        val placename1 = intent.getStringExtra("placename")!!
        buildingName.setText(placename1)
        val options = NaverMapOptions()
            .camera(CameraPosition(LatLng(lat1, lng1), 17.0))
            .mapType(NaverMap.MapType.Basic)
            .zoomControlEnabled(false)//초기 카메라 위치 설정
            .locationButtonEnabled(true)
            .zoomControlEnabled(false)


        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance(options).also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }


        // [START write_message]
        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(placename1)


        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            var f = FloorNumber()
            var rn = RoomNumber()
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                var data = arrayListOf<String>()
                for (snapshot in dataSnapshot.children) {
                    if (snapshot.key.equals("floor")) {
                        for (snapshot2 in snapshot.children) {
                            val btn = Button(this@BuildingInfo).apply {
                                text = snapshot2.key.toString()
                            }
                            for (snapshot3 in snapshot2.children) {
                                rn.room[snapshot3.key.toString()] = snapshot3.getValue(Room::class.java) as Room
                                data.add(snapshot3.child("name").value.toString())
                            }
                            f.rn[snapshot2.key.toString()] = rn


                            test1.addView(btn)
//                            data.add(snapshot2.key.toString())
                            listId.adapter = ArrayAdapter(this@BuildingInfo,android.R.layout.simple_list_item_1,data)
                            Log.d("ttest",snapshot2.toString())
                        }
                    }
                }
                mapFragment.getMapAsync(this@BuildingInfo)
            }


            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
        // [END read_message]


        mapFragment.getMapAsync(this)
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        // ...
    }
}
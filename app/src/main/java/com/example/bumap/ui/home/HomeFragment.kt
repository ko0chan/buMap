package com.example.bumap.ui.home

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import com.example.bumap.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import java.util.*

class HomeFragment : Fragment(), OnMapReadyCallback {
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    var map: HashMap<String, Position> = HashMap<String, Position>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val fm = childFragmentManager

        val options = NaverMapOptions()
            .camera(CameraPosition(LatLng(36.839533958, 127.1846484710), 15.0))
            .mapType(NaverMap. MapType.Basic)
            .zoomControlEnabled(false)//초기 카메라 위치 설정
            .locationButtonEnabled(true)
            .zoomControlEnabled(false)


        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance(options)
                .also {
                    fm.beginTransaction().add(R.id.map, it).commit()
                }//NAVER MAP 객체생성
        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // [START write_message]
        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference


        myRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                var portion:Position
                for (snapshot in dataSnapshot.children) {
                    for (snapshot1 in snapshot.children) {
                        if (snapshot1.key.equals("location")) {
                            portion = snapshot1.getValue((Position::class.java)) as Position
                            portion.placename = snapshot.key.toString()
                            map[portion.placename] = portion
                        }

                    }
                }
                mapFragment.getMapAsync(this@HomeFragment)
            }


            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
        // [END read_message]
        return root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions,
                grantResults
            )
        ) {
            if (!locationSource.isCompassEnabled) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        val infoWindow = InfoWindow()
        infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(this.context!!) {
            override fun getText(infoWindow: InfoWindow): CharSequence {
                // 정보 창이 열린 마커의 tag를 텍스트로 노출하도록 반환
                return infoWindow.marker?.tag as CharSequence? ?: ""
            }
        }

        this.naverMap = naverMap
        naverMap.locationSource = locationSource

        // 마커를 클릭하면:
        val listener = Overlay.OnClickListener { overlay ->
            val marker = overlay as Marker

            if (marker.infoWindow == null) {// 현재 마커에 정보 창이 열려있지 않을 경우 엶
                infoWindow.open(marker)
                infoWindow.onClickListener = Overlay.OnClickListener {
                    val intent = Intent(context,BuildingInfo::class.java)
                    intent.putExtra("placename",marker.tag.toString())
                    intent.putExtra("lat",marker.position.latitude)
                    intent.putExtra("lng",marker.position.longitude)

                    startActivity(intent)

                    true
                }


            } else {// 이미 현재 마커에 정보 창이 열려있을 경우 닫음
                infoWindow.close()
            }

            true
        }
        for (i in map.keys) {
            val marker = Marker()
            marker.position = LatLng(map[i]!!.lat.toDouble(),map.get(i)!!.lng.toDouble())
            marker.map = naverMap
            marker.width = 60
            marker.height = 80
            marker.tag = i
            marker.onClickListener = listener
        }

    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
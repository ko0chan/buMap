package com.example.bumap.ui.home

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
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
import java.util.*

class HomeFragment : Fragment(), OnMapReadyCallback {
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    var map: HashMap<String, Marker> = HashMap<String, Marker>()
    var placeName = arrayOf(
        "학생복지관",
        "목양관",
        "백석홀",
        "인성관",
        "은혜관",
        "자유관",
        "창조관",
        "백석학술정보관",
        "지혜관",
        "진리관",
        "교수회관",
        "음악관",
        "승리관",
        "생활관",
        "글로벌외식산업관",
        "본부동",
        "체육관",
        "조형관",
        "예술대학동"
    )
    var lat = doubleArrayOf(
        36.84067149455031,
        36.84096804048713,
        36.83949817622067,
        36.83943918986522,
        36.83865921671607,
        36.8385077093117,
        36.83750497408212,
        36.83779665070615,
        36.83875974818527,
        36.840167531007694,
        36.83971621180102,
        36.84012975281361,
        36.84180402931098,
        36.84256247647251,
        36.837169093598945,
        36.83930008181875,
        36.841361075498014,
        36.840873386391095,
        36.8387467774056
    )
    var lng = doubleArrayOf(
        127.18245069150657,
        127.18362033438393,
        127.18256704147348,
        127.1835171044122,
        127.18196470541153,
        127.1831779542112,
        127.18230471070564,
        127.1839869000512,
        127.18429855933977,
        127.18453879140225,
        127.18478214426176,
        127.18528504289549,
        127.1857502675112,
        127.18512338682183,
        127.18493789329511,
        127.18597708221137,
        127.1872479173602,
        127.18844000257769,
        127.187425511696
    )


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


        mapFragment.getMapAsync(this)
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
        // [START write_message]
        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference

//        myRef.child("email").setValue("user.email")
//        myRef.child("phone").setValue("user.phoneName")
//        myRef.setValue("Hello, World!")
        // [END write_message]

        // [START read_message]
        // Read from the database
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                val value = dataSnapshot.getValue(String::class.java)
//                Log.d(TAG, "Value is: $value")
                for (snapshot in dataSnapshot.children) {
                    Log.d("ttest1", snapshot.toString())
                    for (snapshot1 in snapshot.children) {
                        Log.d("ttest2", snapshot1.toString())
                        for (snapshot2 in snapshot1.children) {
                            Log.d("ttest3", snapshot2.toString())
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
        // [END read_message]


        val infoWindow = InfoWindow()
        infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(this!!.context!!) {
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
        for (i in placeName.indices) {
            val marker = Marker()
            marker.position = LatLng(lat[i], lng[i])
            marker.map = naverMap
            marker.width = 60
            marker.height = 80
            marker.tag = placeName[i]
            map.set(placeName[i], marker)
            marker.onClickListener = listener


        }

    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
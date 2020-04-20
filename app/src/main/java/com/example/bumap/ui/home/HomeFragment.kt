package com.example.bumap.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.bumap.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource

class HomeFragment : Fragment(), OnMapReadyCallback {
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val fm = childFragmentManager

        val options = NaverMapOptions()
            .camera(CameraPosition(LatLng(36.839533958, 127.1846484710), 15.0))
            .mapType(NaverMap.MapType.Basic)
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
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
    grantResults)) {
        if (!locationSource.isCompassEnabled) { // 권한 거부됨
            naverMap.locationTrackingMode = LocationTrackingMode.None
        }
        return
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
}


    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}

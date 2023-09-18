package com.petpal.mungmate.ui.user

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.petpal.mungmate.MainActivity
import com.petpal.mungmate.R
import com.petpal.mungmate.databinding.FragmentUserInfoBinding
import com.petpal.mungmate.model.UserBasicInfoData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserInfoFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var _fragmentUserInfoBinding: FragmentUserInfoBinding
    private val fragmentUserInfoBinding get() = _fragmentUserInfoBinding
    private lateinit var userViewModel: UserViewModel
    private var userUid = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _fragmentUserInfoBinding = FragmentUserInfoBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity


        return fragmentUserInfoBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        // StateFlow를 사용하여 사용자 데이터 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.user.collect { userData ->
                // userData를 사용하여 사용자 정보 표시
                if (userData != null) {
                    fragmentUserInfoBinding.run {
                        textInputUserNicknameText.setText(userData.displayName)

                        startMainImageView.setImageURI(userData.photoUrl)
                        startMainImageView.tag = userData.photoUrl
                    }

                    //authentication의 uid
                    userUid = userData.uid
                }
            }
        }

        //가입(true)인지 수정(false)인지 식별
        val isRegister = requireArguments().getBoolean("isRegister")

        fragmentUserInfoBinding.run {
            //수정화면이면
            if (!isRegister) {
                //수정완료 버튼 보이기
                userInfoToolbar.inflateMenu(R.menu.complete_menu)
                //툴바 타이틀 내 정보 수정으로 변경
                userInfoToolbar.title = "내 정보 수정"
                //다음 버튼 안보이기
                infoToNextButton.visibility = View.GONE
            }

            userInfoToolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }


            textInputUserBirthText.setOnClickListener {
                // DatePicker 기본값 오늘로 설정
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("날짜 선택")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

                datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
                    val selectDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date(selectedDateInMillis))
                    textInputUserBirthText.setText(selectDate)
                }
                datePicker.show(parentFragmentManager, "tag")
            }

            infoToNextButton.setOnClickListener {
                //firestore에 저장하기
                saveUserData(
                    userUid,
                    startMainImageView.tag as Uri,
                    textInputUserNicknameText.text.toString(),
                    textInputUserBirthText.text.toString(),
                    switchUserInfo.isChecked,
                    getSelectedSex(toggleButtonUserSex.checkedButtonId),
                    getSelectedWalkHour(userInfoRadiogroup.checkedRadioButtonId),
                    textInputStartText.text.toString(),
                    textInputEndText.text.toString()
                ) {
                    mainActivity.navigate(
                        R.id.action_userInfoFragment_to_addPetFragment,
                        bundleOf("isAdd" to true)
                    )
                }

            }

            //언제든 가능해요 옵션을 누르면 산책가능 시간 입력 칸이 없어지도록
            userInfoRadiogroup.setOnCheckedChangeListener { radioGroup, i ->
                if (i == R.id.radioAlways) {
                    linearWhenSelected.visibility = View.GONE
                } else {
                    linearWhenSelected.visibility = View.VISIBLE
                }
            }


        }


    }

    private fun getSelectedWalkHour(checkedRadioButtonId: Int): Availability {
        return if (checkedRadioButtonId == R.id.radioAlways) {
            Availability.WHENEVER
        } else {
            Availability.SPECIFIC
        }
    }

    private fun getSelectedSex(checkedButtonId: Int): Sex {
        return when (checkedButtonId) {
            R.id.buttonMale -> {
                Sex.MALE
            }

            R.id.buttonFemale -> {
                Sex.FEMALE
            }

            else -> {
                Sex.NONE
            }
        }
    }

    //추가하거나 업데이트하기
    private fun saveUserData(
        userUid: String,
        image: Uri? = null,
        nickName: String,
        birth: String,
        isBirthPublic: Boolean,
        checkedSexId: Sex,
        checkedWalkHourId: Availability,
        walkHoursStart: String? = null,
        walkHoursEnd: String? = null,
        successCallBack: () -> Unit,
    ) {
        Log.d(
            "user",
            "${image}, ${nickName}, ${birth}, ${checkedSexId}, ${checkedWalkHourId}, ${walkHoursStart}, $walkHoursEnd"
        )

        val db = Firebase.firestore
        db.collection("users").document(userUid).set(
            UserBasicInfoData(
                image,
                nickName,
                birth,
                isBirthPublic,
                checkedSexId.ordinal,
                checkedWalkHourId.ordinal,
                walkHoursStart,
                walkHoursEnd
            )
        ).addOnSuccessListener {
            Log.d("saveUser", "DocumentSnapshot successfully written!")
            successCallBack()
        }.addOnFailureListener { e -> Log.w("saveUser", "Error writing document", e) }

    }


}

enum class Availability {
    WHENEVER, SPECIFIC
}

enum class Sex {
    MALE, FEMALE, NONE
}

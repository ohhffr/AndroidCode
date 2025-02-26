package com.example.englishgptapplication.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import android.Manifest
import android.content.res.ColorStateList
import android.widget.Button
import android.widget.ImageView
import com.shawnlin.numberpicker.NumberPicker
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.englishgptapplication.R
import com.example.englishgptapplication.databinding.DialogSelectBirthBinding
import com.example.englishgptapplication.databinding.DialogUploadPhotoBinding
import com.example.englishgptapplication.databinding.FragmentMyBinding
import com.example.englishgptapplication.ui.util.ToastUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

//第一个页面功能实现，用户信息管理页面，支持用户编辑头像、昵称、性别和生日，并通过 SharedPreferences 保存和加载用户数据
class MyFragment : Fragment() {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    private lateinit var photoDialog: BottomSheetDialog
    private lateinit var birthDialog: BottomSheetDialog
    private lateinit var currentPhotoPath: String
    private var photoURI: Uri? = null
    private var isBoySelected = true

    // 声明 Activity Result Launchers
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    // SharedPreferences 常量
    companion object {
        const val PREF_USER_INFO = "user_info"
        const val KEY_AVATAR_URI = "avatar_uri"
        const val KEY_GENDER = "gender"
        const val KEY_NICKNAME = "nickname"
        const val KEY_BIRTHDAY = "birthday"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化权限请求
        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                startCamera()
            }
        }

        // 初始化拍照结果 - 使用 TakePicture 合约而不是 StartActivityForResult
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                photoURI?.let {
                    Glide.with(this)
                        .load(it)
                        .into(binding.headPhoto)
                }
            }
        }

        // 初始化选择图片结果
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    Glide.with(this)
                        .load(it)
                        .into(binding.headPhoto)
                    // 保存图片URI到本地变量
                    photoURI = it
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化底部对话框
        initPhotoDialog()
        initBirthDialog()

        // 设置点击事件
        binding.headPhoto.setOnClickListener {
            photoDialog.show()
        }
        binding.etBirth.setOnClickListener {
            birthDialog.show()
        }

        // 设置性别选择的点击事件
        setGenderSelection()

        // 设置返回按钮点击事件
        binding.toolbar.findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            navigateToLibraryFragment()
        }

        // 完成按钮点击事件
        binding.btnFinish.setOnClickListener {
            saveUserInfo()
            ToastUtils.showToast(requireContext(),"修改成功")
            navigateToLibraryFragment()
        }

        // 加载已保存的用户信息
        loadUserInfo()
    }

    private fun navigateToLibraryFragment() {
        // 使用Navigation组件导航回LibraryFragment
        findNavController().navigate(R.id.action_myFragment_to_libraryFragment)
    }

    private fun saveUserInfo() {
        val sharedPreferences = requireContext().getSharedPreferences(PREF_USER_INFO, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            // 保存头像URI
            photoURI?.let { putString(KEY_AVATAR_URI, it.toString()) }

            // 保存性别选择
            putBoolean(KEY_GENDER, isBoySelected)

            // 保存昵称
            putString(KEY_NICKNAME, binding.etName.text.toString())

            // 保存生日
            putString(KEY_BIRTHDAY, binding.etBirth.text.toString())

            apply()
        }
    }

    private fun loadUserInfo() {
        val sharedPreferences = requireContext().getSharedPreferences(PREF_USER_INFO, Context.MODE_PRIVATE)

        // 加载头像
        val savedAvatarUri = sharedPreferences.getString(KEY_AVATAR_URI, null)
        if (savedAvatarUri != null) {
            try {
                val uri = Uri.parse(savedAvatarUri)
                photoURI = uri
                Glide.with(this)
                    .load(uri)
                    .into(binding.headPhoto)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 加载性别选择
        isBoySelected = sharedPreferences.getBoolean(KEY_GENDER, true)
        updateGenderSelection(isBoySelected)

        // 加载昵称
        val savedNickname = sharedPreferences.getString(KEY_NICKNAME, "")
        binding.etName.setText(savedNickname)

        // 加载生日
        val savedBirthday = sharedPreferences.getString(KEY_BIRTHDAY, "")
        binding.etBirth.setText(savedBirthday)
    }

    private fun initPhotoDialog() {
        val dialogBinding = DialogUploadPhotoBinding.inflate(layoutInflater)
        photoDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogBinding.root)
        }

        dialogBinding.root.findViewById<TextView>(R.id.take_photo).setOnClickListener {
            showPermissionDialog(true)
            photoDialog.dismiss()
        }

        dialogBinding.root.findViewById<TextView>(R.id.select_photo).setOnClickListener {
            showPermissionDialog(false)
            photoDialog.dismiss()
        }
    }

    private fun showPermissionDialog(isCamera: Boolean) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("提示")
            .setMessage("适趣APP申请访问存储权限用于修改头像")
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                if (isCamera) openCamera() else openGallery()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .create().apply {
                setOnShowListener {
                    // 自定义按钮样式
                    getButton(AlertDialog.BUTTON_POSITIVE).apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.color_00BFFF))
                    }
                    getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.color_AAA8A8))
                        backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.color_F6F7F8))
                    }
                }
            }.show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 使用新的权限请求API
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startCamera()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        // 使用新的Activity Result API
        pickImageLauncher.launch(intent)
    }

    private fun startCamera() {
        try {
            // 创建用于保存照片的文件
            val photoFile = createImageFile()
            photoFile.also {
                photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.englishgptapplication.fileprovider", // 确保与AndroidManifest中的authorities匹配
                    it
                )
                // 使用TakePicture合约启动相机
                takePictureLauncher.launch(photoURI!!)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // 创建图片文件名
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* 前缀 */
            ".jpg", /* 后缀 */
            storageDir /* 目录 */
        ).apply {
            // 保存文件路径以供使用
            currentPhotoPath = absolutePath
        }
    }

    private fun setGenderSelection() {
        // 默认选中男生
        updateGenderSelection(isBoySelected)

        // 男生选项点击事件
        binding.boy.setOnClickListener {
            isBoySelected = true
            updateGenderSelection(true)
        }

        // 女生选项点击事件
        binding.girl.setOnClickListener {
            isBoySelected = false
            updateGenderSelection(false)
        }
    }

    private fun updateGenderSelection(isBoySelected: Boolean) {
        if (isBoySelected) {
            // 男生选中
            binding.boy.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_gender_checked)
            binding.girl.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_gender_unchecked)
        } else {
            // 女生选中
            binding.boy.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_gender_unchecked)
            binding.girl.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_gender_checked)
        }
    }

    private fun initBirthDialog(){
        val dialogBinding = DialogSelectBirthBinding.inflate(layoutInflater)
        birthDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(dialogBinding.root)
        }
        val yearPicker =  dialogBinding.yearPicker
        val monthPicker =  dialogBinding.monthPicker
        val dayPicker =  dialogBinding.dayPicker


        // 初始化日期选择器
        initDatePickers(yearPicker, monthPicker, dayPicker)

        dialogBinding.btnCancel.setOnClickListener { birthDialog.dismiss() }
        dialogBinding.btnConfirm.setOnClickListener {
            val year = yearPicker.value
            val month = monthPicker.value
            val day = dayPicker.value
            val dateStr = "${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
            binding.etBirth.setText(dateStr)
            birthDialog.dismiss()
        }
    }


    private fun initDatePickers(yearPicker: NumberPicker, monthPicker: NumberPicker, dayPicker: NumberPicker) {
        // 获取当前日期
        val currentDate = Calendar.getInstance(Locale.getDefault())
        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH) + 1 // Calendar 月份从 0 开始
        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)

        // 年份设置（1900-2100）
        yearPicker.minValue = 1900
        yearPicker.maxValue = 2100
        yearPicker.value = currentYear // 设置为当前年份

        // 使用ShawnLin013库的自定义格式化方法
        val yearDisplayedValues = Array(2101 - 1900) { i -> "${i + 1900}年" }
        yearPicker.displayedValues = yearDisplayedValues

        // 月份设置（1-12）
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = currentMonth // 设置为当前月份

        // 月份显示格式
        val monthDisplayedValues = Array(12) { i ->
            if (i + 1 < 10) "0${i + 1}月" else "${i + 1}月"
        }
        monthPicker.displayedValues = monthDisplayedValues

        // 初始化日期
        updateDayPicker(yearPicker.value, monthPicker.value, dayPicker)
        dayPicker.value = currentDay // 设置为当前日

        // 监听年月变化
        yearPicker.setOnValueChangedListener { _, _, newVal ->
            updateDayPicker(newVal, monthPicker.value, dayPicker)
        }
        monthPicker.setOnValueChangedListener { _, _, newVal ->
            updateDayPicker(yearPicker.value, newVal, dayPicker)
        }
    }

    private fun updateDayPicker(year: Int, month: Int, dayPicker: NumberPicker) {
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, 1)
        }
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        dayPicker.minValue = 1
        dayPicker.maxValue = maxDay

        // 日期显示格式
        val dayDisplayedValues = Array(maxDay) { i ->
            if (i + 1 < 10) "0${i + 1}日" else "${i + 1}日"
        }
        dayPicker.displayedValues = dayDisplayedValues

        // 保持当前选择的天数不超过最大值
        if (dayPicker.value > maxDay) {
            dayPicker.value = maxDay
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
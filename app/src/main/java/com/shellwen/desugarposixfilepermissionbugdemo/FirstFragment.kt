package com.shellwen.desugarposixfilepermissionbugdemo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.shellwen.desugarposixfilepermissionbugdemo.databinding.FragmentFirstBinding
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission

data class GetApkPathResult(
    val apkPath: Path,
    val posixFilePermissions: Set<PosixFilePermission>,
    val isSuccess: Boolean
)

fun getApkPath(ctx: Context): GetApkPathResult {
    val apkPath = ctx.packageCodePath
    val path = Path.of(apkPath)
//    val posixFilePermissions = path.getPosixFilePermissions()
    val posixFilePermissions =
        Files.readAttributes(path, "posix:permissions")["permissions"] as Set<*>
    check(posixFilePermissions.all { it is PosixFilePermission }) {
        "posixFilePermissions contains non-PosixFilePermission elements."
    }
    @Suppress("UNCHECKED_CAST")
    return GetApkPathResult(
        apkPath = path,
        posixFilePermissions = posixFilePermissions as Set<PosixFilePermission>,
        isSuccess = true
    )
}

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            kotlin.runCatching {
                getApkPath(requireContext())
            }.fold(
                onSuccess = {
                    binding.textviewFirst.text = """
                        |apkPath: ${it.apkPath}
                        |posixFilePermissions: ${it.posixFilePermissions}
                        |posixFilePermissions.class: ${it.posixFilePermissions.first().javaClass.name}
                        |isSuccess: ${it.isSuccess}
                    """.trimMargin()
                },
                onFailure = { e ->
                    binding.textviewFirst.text = "Error: ${e.stackTraceToString()}"
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
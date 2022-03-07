package com.xayah.materialyoufileexplorer.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import coil.*
import coil.decode.VideoFrameDecoder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xayah.materialyoufileexplorer.ExplorerViewModel
import com.xayah.materialyoufileexplorer.R
import com.xayah.materialyoufileexplorer.databinding.ActivityExplorerBinding
import com.xayah.materialyoufileexplorer.databinding.AdapterFileBinding
import java.io.File


class FileListAdapter(private val mContext: Context, private val model: ExplorerViewModel) :
    RecyclerView.Adapter<FileListAdapter.Holder>() {
    class Holder(val binding: AdapterFileBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var activityBinding: ActivityExplorerBinding

    private var isFile = false

    private val supportExt = setOf(
        /* Video / Container */
        "264", "265", "3g2", "3ga", "3gp", "3gp2", "3gpp", "3gpp2", "3iv", "amr", "asf",
        "asx", "av1", "avc", "avf", "avi", "bdm", "bdmv", "clpi", "cpi", "divx", "dv", "evo",
        "evob", "f4v", "flc", "fli", "flic", "flv", "gxf", "h264", "h265", "hdmov", "hdv",
        "hevc", "lrv", "m1u", "m1v", "m2t", "m2ts", "m2v", "m4u", "m4v", "mkv", "mod", "moov",
        "mov", "mp2", "mp2v", "mp4", "mp4v", "mpe", "mpeg", "mpeg2", "mpeg4", "mpg", "mpg4",
        "mpl", "mpls", "mpv", "mpv2", "mts", "mtv", "mxf", "mxu", "nsv", "nut", "ogg", "ogm",
        "ogv", "ogx", "qt", "qtvr", "rm", "rmj", "rmm", "rms", "rmvb", "rmx", "rv", "rvx",
        "sdp", "tod", "trp", "ts", "tsa", "tsv", "tts", "vc1", "vfw", "vob", "vro", "webm",
        "wm", "wmv", "wmx", "x264", "x265", "xvid", "y4m", "yuv",

        /* Picture */
        "apng", "bmp", "exr", "gif", "j2c", "j2k", "jfif", "jp2", "jpc", "jpe", "jpeg", "jpg",
        "jpg2", "png", "tga", "tif", "tiff", "webp",
    )

    private lateinit var activity: AppCompatActivity

    private fun isThumbnailable (ext: String): Boolean {
        return supportExt.contains(ext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            AdapterFileBinding.inflate(LayoutInflater.from(mContext), parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val current = model.fileList[position]
        val binding = holder.binding
        binding.titleView.text = current.name
        binding.iconView.clear()
        if (current.isDir) {
            if (current.name == "..") {
                binding.iconView.background =
                    AppCompatResources.getDrawable(mContext, R.drawable.ic_round_return)
            } else {
                    binding.iconView.background =
                        AppCompatResources.getDrawable(mContext, R.drawable.ic_round_folder)
            }
        } else {
            val file = File(model.getPath(current.name))
            if (isThumbnailable(file.extension)) {
                binding.iconView.background = null
                binding.iconView.loadAny(file)
                binding.iconView.metadata
            } else {
                binding.iconView.background =
                    AppCompatResources.getDrawable(mContext, R.drawable.ic_round_file)
            }
        }
        binding.content.setOnClickListener {
            val dirName = binding.titleView.text

            if (current.isDir) {
                if (current.name == "..") {
                    model.removePath()
                } else {
                    model.addPath(dirName.toString())
                }
                notifyDataSetChanged()
            } else {
                if (isFile) {
                    MaterialAlertDialogBuilder(activity).setTitle(mContext.getString(R.string.tips))
                        .setMessage(mContext.getString(R.string.query_file))
                        .setNegativeButton(mContext.getString(R.string.cancel)) { _, _ -> }
                        .setPositiveButton(mContext.getString(R.string.confirm)) { _, _ ->
                            model.addPath(dirName.toString())
                            val intent = Intent().apply {
                                putExtra("path", model.getPath())
                                putExtra("isFile", isFile)
                            }
                            activity.setResult(Activity.RESULT_OK, intent)
                            activity.finish()
                        }.show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return model.fileList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun bind(binding: ActivityExplorerBinding) {
        this.activityBinding = binding
    }

    fun init(activity: AppCompatActivity, isFile: Boolean) {
        initializeCoil()
        this.activity = activity
        this.isFile = isFile
    }

    private fun initializeCoil() {
        Coil.setImageLoader(
            ImageLoader.Builder(mContext)
                .componentRegistry {
                    add(VideoFrameDecoder(mContext))
                }
                .build()
        )
    }
}
package edu.washington.hoganc17.clickgen.fragment

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.washington.hoganc17.clickgen.OnUploadListener
import edu.washington.hoganc17.clickgen.R
import edu.washington.hoganc17.clickgen.ServerAPI
import edu.washington.hoganc17.clickgen.ServerApiKotlin
import kotlinx.android.synthetic.main.fragment_upload.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


class UploadFragment : Fragment() {

    private var onUploadListener: OnUploadListener? = null


    companion object {
        val TAG: String = UploadFragment::class.java.simpleName
        const val FILE_CHOICE_CODE = 4307
        private const val BASE_URL = "http://174.21.95.118:5000/"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnUploadListener) {
            onUploadListener = context
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnUploadFile.setOnClickListener {
            //onUploadListener?.onFileUploaded()
            selectFile()
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "audio/*"
        val i = Intent.createChooser(intent, "File")
        startActivityForResult(i, FILE_CHOICE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_CHOICE_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            uri?.let {
                uploadFile(it)
            }
        }
    }

    private fun uploadFile(uri: Uri) {
        val file = File("" + uri)
        val fileType = context?.contentResolver?.getType(uri)

        val descPart = RequestBody.create(MultipartBody.FORM, "description")

        val filePart = RequestBody.create(
                MediaType.parse(fileType!!),
                file
        )

        val multiPart = MultipartBody.Part.createFormData("Song", file.name, filePart)

        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val serverAPI = retrofit.create(ServerApiKotlin::class.java)

        val call = serverAPI.postSong(descPart, multiPart)
    }
}

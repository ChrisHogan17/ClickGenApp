package edu.washington.hoganc17.clickgen.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.washington.hoganc17.clickgen.model.OnUploadListener
import edu.washington.hoganc17.clickgen.R
import edu.washington.hoganc17.clickgen.model.AudioPair
import edu.washington.hoganc17.clickgen.model.FileUploadUtils
import kotlinx.android.synthetic.main.fragment_upload.*
import java.io.FileInputStream


class UploadFragment : Fragment() {

    private var onUploadListener: OnUploadListener? = null


    companion object {
        val TAG: String = UploadFragment::class.java.simpleName
        const val FILE_CHOICE_CODE = 4307
        private const val URL = "http://174.21.95.118:5000/generate"
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
             val theDust = resources.openRawResource(R.raw.bites_dust_16)
//            val uri = data?.data?.path
//            uri?.let {
//                Log.i("JOSUKE", it)
//            }*/

            //val test = FileUploadUtils.generate(theDust, URL)
            //Log.i("GNOME", test.sr.toString())
        }
    }
}

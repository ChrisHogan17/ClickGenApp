package edu.washington.hoganc17.clickgen.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import edu.washington.hoganc17.clickgen.MainActivity
import edu.washington.hoganc17.clickgen.R
import edu.washington.hoganc17.clickgen.model.FileUploadUtils
import edu.washington.hoganc17.clickgen.model.OnUploadListener
import kotlinx.android.synthetic.main.fragment_upload.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


class UploadFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var onUploadListener: OnUploadListener? = null
    private var clickFrequency = 880.0f
    private var clickDuration = 0.5f

    companion object {
        val TAG: String = UploadFragment::class.java.simpleName
        const val FILE_CHOICE_CODE = 4307
        private const val CONVERT_SONG_URL = "http://174.21.95.118:5000/convert"
        private const val CONVERT_SONG_LOCAL_URL = "http://192.168.0.76:5000/convert"

        private const val GENERATE_CLICK_URL = "http://174.21.95.118:5000/generate"
        private const val GENERATE_CLICK_LOCAL_URL = "http://192.168.0.76:5000/generate"

        private const val GENERATE_FULL_URL = "http://174.21.95.118:5000/generateFull"
        private const val GENERATE_FULL_LOCAL_URL = "http://192.168.0.76:5000/generateFull"

        private const val GENERATE_TWO_URL = "http://174.21.95.118:5000/generateTwo"
        private const val GENERATE_TWO_LOCAL_URL = "http://192.168.0.76:5000/generateTwo"

        private const val GENERATE_MIXED_URL = "http://174.21.95.118:5000/generateMixed"
        private const val GENERATE_MIXED_LOCAL_URL = "http://192.168.0.76:5000/generateMixed"

        private const val GET_FILE_URL = "http://174.21.95.118:5000/get"
        private const val GET_FILE_LOCAL_URL = "http://192.168.0.76:5000/get"


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
            selectFile()
        }

        context?.let {
            val freqAdapter = ArrayAdapter.createFromResource(it, R.array.frequencies, android.R.layout.simple_spinner_item)
            freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFrequency.adapter = freqAdapter

            spinnerFrequency.onItemSelectedListener = this

            val durationAdapter = ArrayAdapter.createFromResource(it, R.array.durations, android.R.layout.simple_spinner_item)
            durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDuration.adapter = durationAdapter

            spinnerDuration.onItemSelectedListener = this
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
            // Change setup UI to loading UI
            switchUi()

            val uri = data?.data

            uri?.let {
                val inputStream: InputStream? = context?.contentResolver?.openInputStream(it)
                val name = getFileName(it)

                doAsync {
                    try {

                        val mixedInputStream: InputStream = FileUploadUtils.requestFile(inputStream, GENERATE_MIXED_LOCAL_URL, name, clickFrequency, clickDuration)
                        Log.i("CASHEW", "Got mixed auduio")

                        uiThread {
                            onUploadListener?.onFileUploaded(mixedInputStream)
                        }

                    } catch (ex: Exception) {
                        when (ex) {
                            is IOException, is NullPointerException, is JSONException -> {
                                Log.i("HULK", ex.toString())
                            }
                            else -> throw ex
                        }
                    }
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val itemContent = parent?.getItemAtPosition(position).toString()
        val numericalVal = itemContent.split(" ")[0]
        Log.i("BRIT", numericalVal)
        if(parent?.id == R.id.spinnerDuration) {
            clickDuration = numericalVal.toFloat()
        } else if (parent?.id == R.id.spinnerFrequency) {
            clickFrequency = numericalVal.toFloat()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    private fun switchUi(){
        btnUploadFile.visibility = View.GONE
        tvAppDescription.visibility = View.GONE
        tvUploadInstructions.visibility = View.GONE
        spinnerFrequency.visibility = View.GONE
        spinnerDuration.visibility = View.GONE
        tvClickSettings.visibility = View.GONE
        tvFrequency.visibility = View.GONE
        tvDuration.visibility = View.GONE

        tvPleaseWait.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
    }

    // Code from: https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content/53170119
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)
            cursor.use { it ->
                if (it != null && it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                if (cut != null) {
                    result = result?.substring(cut + 1)
                }
            }
        }
        return result
    }
}

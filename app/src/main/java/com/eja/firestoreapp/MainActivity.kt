package com.eja.firestoreapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val mahasiswaCollectionRef = Firebase.firestore.collection("mahasiswa")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnUploadData.setOnClickListener {
            val mahasiswa = getOldMahasiswa()
            saveMahasiswa(mahasiswa)
        }

        btnUpdateMahasiswa.setOnClickListener {
            val dataMahasiswaLama = getOldMahasiswa()
            val dataMahasiswaBaru = getMahasiswaBaru()
            updateMahasiswa(dataMahasiswaLama, dataMahasiswaBaru)
        }

        btnDeleteMahasiswa.setOnClickListener {
            val mahasiswa = getOldMahasiswa()
            deleteMahasiswa(mahasiswa)
        }

        btnRetrieveData.setOnClickListener {
            retrieveMahasiswa()
        }


    }

    private fun getOldMahasiswa(): Mahasiswa {
        val nama = etNama.text.toString()
        val jurusan = etJurusan.text.toString()
        val asal = etAsal.text.toString()
        return Mahasiswa(nama, jurusan, asal)
    }

    private fun getMahasiswaBaru(): Map<String, Any> {
        val nama = etNamaBaru.text.toString()
        val jurusan = etJurusanBaru.text.toString()
        val asal = etAsalBaru.text.toString()
        val map = mutableMapOf<String, Any>()
        if (nama.isNotEmpty()) {
            map["nama"] = nama
        }
        if (jurusan.isNotEmpty()) {
            map["jurusan"] = jurusan
        }
        if (asal.isNotEmpty()) {
            map["asal"] = asal
        }
        return map
    }

    private fun deleteMahasiswa(mahasiswa: Mahasiswa) = CoroutineScope(Dispatchers.IO).launch {
        val mahasiswaData = mahasiswaCollectionRef
            .whereEqualTo("nama", mahasiswa.nama)
            .whereEqualTo("jurusan", mahasiswa.jurusan)
            .whereEqualTo("asal", mahasiswa.asal)
            .get()
            .await()
        if (mahasiswaData.documents.isNotEmpty()) {
            for (data in mahasiswaData) {
                try {
                    mahasiswaCollectionRef.document(data.id).delete().await()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "No persons matched the query.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun updateMahasiswa(mahasiswa: Mahasiswa, mahasiswaBaru: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {
            val mahasiswaData = mahasiswaCollectionRef
                .whereEqualTo("nama", mahasiswa.nama)
                .whereEqualTo("jurusan", mahasiswa.jurusan)
                .whereEqualTo("asal", mahasiswa.asal)
                .get()
                .await()
            if (mahasiswaData.documents.isNotEmpty()) {
                for (data in mahasiswaData) {
                    try {
                        mahasiswaCollectionRef.document(data.id)
                            .set(
                                mahasiswaBaru, SetOptions.merge()
                            ).await()
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "No persons matched the query.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun retrieveMahasiswa() = CoroutineScope(Dispatchers.IO).launch {
        var dariAsal = etDari.text.toString()
        var keAsal = etKe.text.toString()
        try {
            val dataHasil = mahasiswaCollectionRef
                .whereEqualTo("asal", dariAsal)
                .whereEqualTo("asal", keAsal)
                .get()
                .await()
            val sb = StringBuilder()
            for (hasil in dataHasil.documents) {
                val mahasiswa = hasil.toObject<Mahasiswa>()
                sb.append("$mahasiswa\n")
            }
            withContext(Dispatchers.Main) {
                tvMahasiswa.text = sb.toString()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveMahasiswa(mahasiswa: Mahasiswa) = CoroutineScope(Dispatchers.IO).launch {

        try {
            mahasiswaCollectionRef.add(mahasiswa).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "Sukses menyimpan data mahasiswa",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
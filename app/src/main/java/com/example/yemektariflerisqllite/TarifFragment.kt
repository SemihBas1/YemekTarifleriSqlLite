package com.example.yemektariflerisqllite

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_tarif.*
import java.io.ByteArrayOutputStream


class TarifFragment : Fragment() {


    var secilenGorsel: Uri?=null
    var secilenBitmap:Bitmap?=null





    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view= inflater.inflate(R.layout.fragment_tarif, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        semih.setOnClickListener{
            kaydet(it)


        }
        imageView.setOnClickListener {
            gorselSec(it)

        }

        arguments?.let {
             var gelenbilgi=TarifFragmentArgs.fromBundle(it).bilgi
            if(gelenbilgi.equals("menudengeldim")){
                yemekIsmiText.setText("")
                yemekMalzemeText.setText("")
                semih.visibility=View.VISIBLE
                val gorselSecmeArkaplani=BitmapFactory.decodeResource(context?.resources,R.drawable.gorsel)
                imageView.setImageBitmap(gorselSecmeArkaplani)


            }else{
                semih.visibility=View.INVISIBLE
                val secilenId=TarifFragmentArgs.fromBundle(it).id
                context?.let {
                    try {
                        val db=it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                        val cursor=db.rawQuery("SELECT * FROM yemekler where id=?", arrayOf(secilenId.toString()))
                        val yemekIsmiIndex =cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeIndex=cursor.getColumnIndex("yemekmalzemesi")
                        val yemekGorseli=cursor.getColumnIndex("gorsel")
                        while(cursor.moveToNext()){
                            yemekIsmiText.setText(cursor.getString(yemekIsmiIndex))
                            yemekMalzemeText.setText(cursor.getString(yemekMalzemeIndex))
                            val byteDizisi= cursor.getBlob(yemekGorseli)
                            val bitmap=BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                            imageView.setImageBitmap(bitmap)

                        }
                        cursor.close()

                    }catch (e:Exception){
                        e.printStackTrace()

                    }
                }

            }

        }





        
    }
    fun kaydet(view: View){
        println("tıklandı")
        val yemekIsmi=yemekIsmiText.text.toString()
        val yemekMalzemeleri=yemekMalzemeText.text.toString()
        if(secilenBitmap != null){

            val kucukBitmap=kucukBitmapOlustur(secilenBitmap!!,300)
            val outputStream=ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi=outputStream.toByteArray()
            try {
                context?.let {
                    val database=it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null )
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler(id INTEGER PRIMARY KEY,yemekismi VARCHAR,yemekmalzemesi VARCHAR,gorsel BLOB)")
                    val sqlString="INSERT INTO yemekler(yemekismi,yemekmalzemesi,gorsel) VALUES(?,?,?)"
                    val statament=database.compileStatement(sqlString)
                    statament.bindString(1,yemekIsmi)
                    statament.bindString(2,yemekMalzemeleri)
                    statament.bindBlob(3,byteDizisi)
                    statament.execute()


                }


            }catch (e:Exception ){



            }
        }
        val action=TarifFragmentDirections.actionTarifFragmentToListeFragment2()
        Navigation.findNavController(view).navigate(action)







    }
    fun gorselSec(view: View){
        println("gorsel seçildi")


        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
                val galeriIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)





            }else{
                val galeriIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)


            }


        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==1){
            if (grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                val galeriIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)


            }


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==2 && resultCode== Activity.RESULT_OK && data != null){
            secilenGorsel=data.data
            try {
                context?.let {
                    if(secilenGorsel!=null&&context!=null){

                        val source =ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                        secilenBitmap=ImageDecoder.decodeBitmap(source)
                        imageView.setImageBitmap(secilenBitmap)


                    }
                }



            }catch (e:java.lang.Exception){
                e.printStackTrace()


            }


        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun kucukBitmapOlustur(kullanicininSectigiBitmap:Bitmap,maximumBoyut:Int):Bitmap{
        var width=kullanicininSectigiBitmap.width
        var height=kullanicininSectigiBitmap.height

        val bitmapOrani: Double=width.toDouble()/height.toDouble()
        if(bitmapOrani>1){
            width=maximumBoyut
            val kisaltilmisheight = width/bitmapOrani
            height=kisaltilmisheight.toInt()


        }else{
            height=maximumBoyut
            val kisaltilmiswidth= height*bitmapOrani
            width=kisaltilmiswidth.toInt()

        }


        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)

    }


}
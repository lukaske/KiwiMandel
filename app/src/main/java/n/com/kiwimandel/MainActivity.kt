package n.com.kiwimandel
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.yuyakaido.android.cardstackview.*
import org.json.JSONObject
import java.util.*
import androidx.core.os.HandlerCompat.postDelayed



class MainActivity : AppCompatActivity(), CardStackListener {

    var animZoomIn: Animation? = null
    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val cardStackView by lazy { findViewById<CardStackView>(R.id.card_stack_view) }
    private val manager by lazy { CardStackLayoutManager(this, this) }
    private val adapter by lazy { CardStackAdapter(createSpots(), this) }
    private val flightmanager by lazy { FlightManager(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val imgPoster: ImageView? = findViewById(R.id.tunnelview)
        val animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin);
        val animZoomIn3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom3)
        val btn = findViewById(R.id.button) as Button


        flightmanager.listener = {
            flightmanager.log("Flightmanager listener engaged")
            Handler().postDelayed(Runnable {
                setupCardStackView()
            }, 1250)
            Handler().postDelayed(Runnable {
                imgPoster?.startAnimation(animZoomIn3)
                imgPoster?.animate()!!.alpha(0.0f)
            }, 1300)



        }

        imgPoster?.startAnimation(animZoomIn);
        Glide.with(this)
            .load(getResources().getIdentifier("tunnel", "drawable", this.getPackageName()))
            .into(findViewById(R.id.tunnelview));


        btn.setOnClickListener {
            if(ConnectivityHelper().isConnectedToNetwork(this)){
                btn.visibility = View.GONE
                flightmanager.dataRun()
            }else{
                Toast.makeText(this, "Couldn't connect. Restart to retry.", Toast.LENGTH_SHORT).show()
            }

        }



    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }



    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        Log.d("CardStackView", "onCardSwiped: p = ${manager.topPosition}, d = $direction")
        if (manager.topPosition == adapter.itemCount - 1) {
            paginate()
        }
    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")
    }

    override fun onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardAppeared: ($position) ${textView.text}")
    }

    override fun onCardDisappeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardDisappeared: ($position) ${textView.text}")
    }

    private fun setupCardStackView() {
        initialize()
    }

    private fun initialize() {
        manager.setStackFrom(StackFrom.None)
        manager.setVisibleCount(3)
        manager.setTranslationInterval(8.0f)
        manager.setScaleInterval(0.95f)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(true)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        cardStackView.layoutManager = manager
        cardStackView.adapter = adapter
        cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    private fun paginate() {
        val old = adapter.getSpots()
        val new = old.plus(createSpots())
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun createSpots(): List<Spot> {
        val spots = ArrayList<Spot>()
        val source = flightmanager.api_data()
        for (x in 0 until source.length()){
            val json = JSONObject(source[x].toString())
            spots.add(Spot(name = "${json.getString("cityTo")}, ${json.getJSONObject("countryTo").getString("name")}", city = "Flying from ${json.getString("cityFrom")} for ${json.getString("price")}€" , url = "https://images.kiwi.com/photos/600x330/${json.getString("mapIdto")}.jpg", book=json.getString("deep_link")))
            flightmanager.log(x.toString())
        }

        return spots
    }




}
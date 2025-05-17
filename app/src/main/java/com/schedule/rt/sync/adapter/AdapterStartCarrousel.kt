import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R

class AdapterStartCarrousel(private val imageResIds: List<Int>) :
    RecyclerView.Adapter<AdapterStartCarrousel.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemCarrousel: ImageView = itemView.findViewById(R.id.ivStartCarrousel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_start_carrousel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageResId = imageResIds[position % imageResIds.size] // Looping tak terbatas
        holder.itemCarrousel.setImageResource(imageResId)
    }

    override fun getItemCount(): Int = Int.MAX_VALUE // Untuk looping tak terbatas
}
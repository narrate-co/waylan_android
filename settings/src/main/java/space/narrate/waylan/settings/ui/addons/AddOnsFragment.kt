package space.narrate.waylan.settings.ui.addons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.common.BaseFragment
import space.narrate.waylan.settings.R
import space.narrate.waylan.settings.databinding.FragmentAddOnsBinding

class AddOnsFragment : BaseFragment() {

    private lateinit var binding: FragmentAddOnsBinding

    private val navigator: Navigator by inject()

    private val viewModel: AddOnsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddOnsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            appBar.doOnElasticDrag(
                alphaViews = listOf(scrollView, appBar)
            )
            appBar.doOnElasticDismiss {
                navigator.toBack(Navigator.BackType.DRAG, this.javaClass.simpleName)
            }
            appBar.setOnNavigationIconClicked {
                navigator.toBack(Navigator.BackType.ICON, this.javaClass.simpleName)
            }
            appBar.setReachableContinuityNavigator(this@AddOnsFragment, navigator)

            val adapter = AddOnsPreviewAdapter()
            recyclerView.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            recyclerView.isNestedScrollingEnabled = false
            val snapScrollEventAdapter = SnapScrollEventAdapter(recyclerView)
            snapScrollEventAdapter.setOnPageChangedCallback(
                object : SnapScrollEventAdapter.OnPageChangedCallback {
                    override fun onPageSelected(position: Int) {
                        val item = adapter.getItemAt(position)
                        viewModel.onCurrentAddOnPageChanged(item)
                    }
                }
            )
            recyclerView.addOnScrollListener(snapScrollEventAdapter)
            recyclerView.adapter = adapter

            viewModel.currentAddOn.observe(this@AddOnsFragment) {
                updateDescriptionArea(it)
            }

            viewModel.addOns.observe(this@AddOnsFragment) {
                adapter.submitList(it)
            }
        }

        startPostponedEnterTransition()
    }

    private fun updateDescriptionArea(addOn: AddOnItemModel) {
        binding.run {
            descriptionTextView.text = getString(addOn.desc)
            // TODO: Update current status text label.

            actionsContainer.removeAllViews()
            addOn.state.actions.forEach { action ->
                val button = LayoutInflater.from(actionsContainer.context).inflate(
                    R.layout.add_on_action_button_item,
                    actionsContainer,
                    false
                ) as Button
                button.text = getString(action.title)
                button.setOnClickListener { viewModel.onActionClicked(addOn, action) }
                actionsContainer.addView(button)
            }
        }
    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        binding.run {
            coordinatorLayout.updatePadding(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight
            )
            scrollView.updatePadding(bottom = insets.systemWindowInsetBottom)
        }
        return super.handleApplyWindowInsets(insets)
    }
}
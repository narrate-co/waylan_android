package space.narrate.waylan.settings.ui.addons

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.core.billing.BillingManager
import space.narrate.waylan.core.data.firestore.users.state
import space.narrate.waylan.core.data.firestore.users.statusTextLabel
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.util.configError
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.launchEmail
import space.narrate.waylan.core.util.make
import space.narrate.waylan.core.util.visible
import space.narrate.waylan.settings.R
import space.narrate.waylan.settings.databinding.FragmentAddOnsBinding
import space.narrate.waylan.settings.ui.dialog.MessageAlertDialog
import space.narrate.waylan.settings.ui.settings.SettingsFragment

/**
 * A Fragment that shows a horizontal list of [AddOn]s, the stat of each add on for the current
 * user, and any actions available to be taken on those add-ons.
 */
class AddOnsFragment : Fragment() {

    private lateinit var binding: FragmentAddOnsBinding

    private val billingManager: BillingManager by inject()

    private val navigator: Navigator by inject()

    private val viewModel: AddOnsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
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
            appBar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.add_on_menu_info -> {
                        showAboutDialog()
                        true
                    }
                    else -> false
                }
            })
            appBar.setReachableContinuityNavigator(this@AddOnsFragment, navigator)

            viewModel.setShowOnOpenAddOn(navArgs<AddOnsFragmentArgs>().value.addOn)

            viewModel.shouldScrollToPosition.observe(this@AddOnsFragment) { event ->
                event.withUnhandledContent {
                    when (val manager = recyclerView.layoutManager) {
                        is LinearLayoutManager -> {
                            manager.scrollToPositionWithOffset(it, 0)
                            viewModel.onCurrentAddOnPageChanged(it)
                        }
                    }
                }
            }

            val adapter = AddOnsPreviewAdapter()
            recyclerView.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            recyclerView.setHasFixedSize(true)
            val snapScrollEventAdapter = SnapScrollEventAdapter(recyclerView)
            snapScrollEventAdapter.setOnPageChangedCallback(
                object : SnapScrollEventAdapter.OnPageChangedCallback {
                    override fun onPageSelected(position: Int) {
                        viewModel.onCurrentAddOnPageChanged(position)
                    }
                }
            )
            recyclerView.addOnScrollListener(snapScrollEventAdapter)
            recyclerView.addItemDecoration(DotIndicatorDecoration(requireContext()))
            recyclerView.adapter = adapter


            viewModel.currentAddOn.observe(this@AddOnsFragment) {
                updateDescriptionArea(it)
            }

            viewModel.shouldShowStatusTextLabel.observe(this@AddOnsFragment) {
                if (it) textLabel.visible() else textLabel.gone()
            }

            viewModel.addOns.observe(this@AddOnsFragment) {
                adapter.submitList(it)
            }
        }

        billingManager.billingEvent.observe(this) { event ->
            event.withUnhandledContent {
                viewModel.onBillingEvent(it)
            }
        }

        viewModel.shouldShowSnackbar.observe(this) { event ->
            event.withUnhandledContent {
                it.make(binding.coordinatorLayout).show()
            }
        }

        viewModel.shouldLaunchPurchaseFlow.observe(this) { event ->
            event.withUnhandledContent {
                billingManager.initiatePurchaseFlow(
                    requireActivity(),
                    it.addOn,
                    it.addOnAction
                )
            }
        }

        viewModel.shouldShowAccountRequiredDialog.observe(this) { event ->
            event.withUnhandledContent {
                MessageAlertDialog(
                    requireContext(),
                    R.string.add_on_account_required_body,
                    positiveButton = R.string.add_on_account_required_log_in_button,
                    positiveAction = { viewModel.onAccountRequiredLogInClicked() },
                    negativeButton = R.string.add_on_account_required_sign_up_button,
                    negativeAction = { viewModel.onAccountRequiredSignUpClicked() }
                ).show()
            }
        }

        viewModel.shouldLaunchLogIn.observe(this) { event ->
            event.withUnhandledContent { navigator.toLogIn(requireContext()) }
        }

        viewModel.shouldLaunchSignUp.observe(this) { event ->
            event.withUnhandledContent { navigator.toSignUp(requireContext()) }
        }

        startPostponedEnterTransition()
    }

    private fun updateDescriptionArea(addOn: AddOnItemModel) {
        binding.run {
            val status = addOn.userAddOn.statusTextLabel(requireContext())
            textLabel.text = status
            descriptionTitle.text = getString(addOn.descTitle)
            descriptionBody.text = getString(addOn.descBody)

            actionsContainer.removeAllViews()
            addOn.userAddOn.state.actions.forEach { action ->
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

    private fun showAboutDialog() {
        MessageAlertDialog(
            requireContext(),
            R.string.add_on_about_body,
            R.string.add_on_about_contact_button_title,
            {
                try {
                    requireContext().launchEmail(
                        SettingsFragment.SUPPORT_EMAIL_ADDRESS,
                        getString(R.string.add_on_email_contact_subject)
                    )
                } catch (e: ActivityNotFoundException) {
                    Snackbar.make(
                        binding.coordinatorLayout,
                        R.string.settings_email_compose_no_client_error,
                        Snackbar.LENGTH_SHORT
                    )
                        .configError(requireContext())
                        .show()
                }
            }
        ).show()
    }
}
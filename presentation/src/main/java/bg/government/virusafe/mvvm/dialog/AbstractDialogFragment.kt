package bg.government.virusafe.mvvm.dialog

import androidx.databinding.ViewDataBinding
import bg.government.virusafe.mvvm.viewmodel.AbstractViewModel
import com.upnetix.presentation.view.BaseDialogFragment

/**
 * Abstract class for creating dialog fragments.
 *
 * @param B  the dialog fragment binding type
 * @param VM the view model
 *
 * @author stoyan.yanev
 */
abstract class AbstractDialogFragment<B : ViewDataBinding, VM : AbstractViewModel> : BaseDialogFragment<B, VM>()

package bg.government.virusafe.mvvm.di

import bg.government.virusafe.app.MainActivity
import bg.government.virusafe.app.WebViewFragment
import bg.government.virusafe.app.appinfo.AppInfoFragment
import bg.government.virusafe.app.home.HomeFragment
import bg.government.virusafe.app.home.TermsAndConditionsDialog
import bg.government.virusafe.app.localization.LocalizationFragment
import bg.government.virusafe.app.registration.CodeVerificationFragment
import bg.government.virusafe.app.personaldata.PersonalDataFragment
import bg.government.virusafe.app.registration.RegistrationFragment
import bg.government.virusafe.app.selfcheck.DoneFragment
import bg.government.virusafe.app.selfcheck.SelfCheckFragment
import bg.government.virusafe.app.splash.SplashActivity
import com.upnetix.presentation.di.module.BaseAppModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Main app module for providing dependencies.
 *
 * @author stoyan.yanev
 */
@Module
abstract class AppModule : BaseAppModule() {

	@ContributesAndroidInjector
	abstract fun splashActivity(): SplashActivity

	@ContributesAndroidInjector
	abstract fun mainActivity(): MainActivity

	@ContributesAndroidInjector
	abstract fun registrationFragment(): RegistrationFragment

	@ContributesAndroidInjector
	abstract fun selfCheckFragment(): SelfCheckFragment

	@ContributesAndroidInjector
	abstract fun codeVerificationFragment(): CodeVerificationFragment

	@ContributesAndroidInjector
	abstract fun homeFragment(): HomeFragment

	@ContributesAndroidInjector
	abstract fun doneFragment(): DoneFragment

	@ContributesAndroidInjector
	abstract fun termsAndConditionsDialog(): TermsAndConditionsDialog

	@ContributesAndroidInjector
	abstract fun webViewFragment(): WebViewFragment

	@ContributesAndroidInjector
	abstract fun personalNoFragment(): PersonalDataFragment

	@ContributesAndroidInjector
	abstract fun appInfoFragment(): AppInfoFragment

	@ContributesAndroidInjector
	abstract fun localizationFragment(): LocalizationFragment
}

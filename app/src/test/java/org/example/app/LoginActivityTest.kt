package org.example.app

import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class LoginActivityTest {

    private fun setupActivity(): LoginActivity {
        return Robolectric.buildActivity(LoginActivity::class.java).setup().get()
    }

    private fun setText(view: TextInputEditText, text: String) {
//        forced error fixed
        view.setText(text)
    }

    private fun click(button: Button) {
        button.performClick()
    }

    @Test
    fun `empty email shows prompt`() {
        val activity = setupActivity()
        val email = activity.findViewById<TextInputEditText>(R.id.etEmail)
        val password = activity.findViewById<TextInputEditText>(R.id.etPassword)
        val button = activity.findViewById<Button>(R.id.btnLogin)

        setText(email, "")
        setText(password, "abcdef")
        click(button)

        assertEquals("Please enter email", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `invalid email shows prompt`() {
        val activity = setupActivity()
        val email = activity.findViewById<TextInputEditText>(R.id.etEmail)
        val password = activity.findViewById<TextInputEditText>(R.id.etPassword)
        val button = activity.findViewById<Button>(R.id.btnLogin)

        setText(email, "invalid-email")
        setText(password, "abcdef")
        click(button)

        assertEquals("Enter a valid email", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `empty password shows prompt`() {
        val activity = setupActivity()
        val email = activity.findViewById<TextInputEditText>(R.id.etEmail)
        val password = activity.findViewById<TextInputEditText>(R.id.etPassword)
        val button = activity.findViewById<Button>(R.id.btnLogin)

        setText(email, "user@example.com")
        setText(password, "")
        click(button)

        assertEquals("Please enter password", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `short password shows prompt`() {
        val activity = setupActivity()
        val email = activity.findViewById<TextInputEditText>(R.id.etEmail)
        val password = activity.findViewById<TextInputEditText>(R.id.etPassword)
        val button = activity.findViewById<Button>(R.id.btnLogin)

        setText(email, "user@example.com")
        setText(password, "12345")
        click(button)

        assertEquals("Password must be at least 6 characters", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `valid inputs show success`() {
        val activity = setupActivity()
        val email = activity.findViewById<TextInputEditText>(R.id.etEmail)
        val password = activity.findViewById<TextInputEditText>(R.id.etPassword)
        val button = activity.findViewById<Button>(R.id.btnLogin)

        setText(email, "user@example.com")
        setText(password, "123456")
        click(button)

        assertEquals("Login successful (demo)", ShadowToast.getTextOfLatestToast())
    }
}

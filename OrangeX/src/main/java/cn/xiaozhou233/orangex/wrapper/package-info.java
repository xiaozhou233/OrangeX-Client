/**
 * Cross-version wrapper.
 *
 * <ul>
 *   <li>{@code api} - stable contracts consumed by feature code. Depends on
 *       the JDK only; never on Minecraft or mapping names.</li>
 *   <li>{@code common} - version-independent value objects shared by the API
 *       and implementations.</li>
 *   <li>{@code internal} - version selection ({@code WrapperRegistry},
 *       {@code WrapperContext}) and one {@code vX_Y_Z} package per supported
 *       Minecraft version.</li>
 * </ul>
 *
 * <p>Only {@code internal.vX_Y_Z} implementations may reference Minecraft
 * classes or mapping names. Feature code goes through {@code api} and creates
 * wrappers via {@link cn.xiaozhou233.orangex.wrapper.MinecraftFactory}.</p>
 *
 * <p>To support a new Minecraft version: add a {@code WrapperProvider} under
 * {@code internal.vX_Y_Z} and register it in
 * {@link cn.xiaozhou233.orangex.wrapper.internal.WrapperRegistry}.</p>
 */
package cn.xiaozhou233.orangex.wrapper;

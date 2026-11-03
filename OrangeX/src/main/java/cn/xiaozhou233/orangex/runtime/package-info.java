/**
 * Minecraft runtime detection.
 *
 * <p>Detection flows in a fixed order:</p>
 * <ol>
 *   <li>{@link cn.xiaozhou233.orangex.runtime.classloader.GameClassLoaderResolver}
 *       finds the game class loader.</li>
 *   <li>{@link cn.xiaozhou233.orangex.runtime.loader.LoaderDetector}
 *       identifies the mod loader.</li>
 *   <li>{@link cn.xiaozhou233.orangex.runtime.version.VersionDetector}
 *       resolves the internal version id: loader metadata, then 26.x
 *       {@code version.json}, then structural signatures.</li>
 * </ol>
 *
 * <p>Extending detection:</p>
 * <ul>
 *   <li>New mod loader: add a {@code LoaderProbe} to {@code LoaderDetector}.</li>
 *   <li>New Minecraft version: add a {@code VersionSignature} to
 *       {@code VersionSignatureRegistry} (and an enum value if needed).</li>
 *   <li>New loader version metadata: add a {@code VersionIdSource} to
 *       {@code VersionIdSources}.</li>
 * </ul>
 */
package cn.xiaozhou233.orangex.runtime;

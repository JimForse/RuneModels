This mod handles the rendering of textures, models, and animations-both custom assets (located via the path `/runemodels/assets/runemodels/`, with subfolders for `animations/`, `geo/`, and `textures/`) and assets provided by other mods (following the same structure, but placed within the `resources/` folder of the respective mod).

If you are a mod creator, you can change the directory where your mod files are located by using the `rw.modden.api.Paths` class from the `main` branch-specifically, its `setRoot(Path newRoot)` method. I have also included Javadoc comments wherever I felt they were necessary, so feel free to take a look.
Alternatively, you can (as mentioned above) simply create the folders `animations/`, `geo/`, and `textures/` directly inside your mod's `resources/` folder.

This mod includes the following server commands:
1. /runemodels set 'targets' 'name'                      (texture + model, if it exists, by one name)
2. /runemodels set 'targets' model 'model' ['texture']   (model; the texture of the same name if not given)
3. /runemodels set 'targets' texture 'texture'           (texture only, uses the vanilla model)
4. /runemodels animlist set 'targets' 'animation_set'
5. /runemodels animlist remove 'targets'
6. /runemodels clear 'targets'
7. /runemodels play 'targets' 'animation' [loop|once|hold]
8. /runemodels stop 'targets'

If you want, you can use it in your game, to render:
- texture;
- models with texture;
- model with texture and with animation;
- animations, once the models and textures have already been rendered;

This mod was created specifically for the minecraft project AxoRune.
During development, the Claude neural network (Sonnet 5.5 model) was used. 
However, the code was verified both before and after its use.